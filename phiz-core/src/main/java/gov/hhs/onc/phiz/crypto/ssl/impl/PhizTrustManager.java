package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.crypto.logging.SslTrustEvent;
import gov.hhs.onc.phiz.crypto.logging.impl.SslTrustEventImpl;
import gov.hhs.onc.phiz.crypto.ssl.PhizSslLocation;
import gov.hhs.onc.phiz.crypto.ssl.PhizSslManagerBean;
import gov.hhs.onc.phiz.crypto.ssl.constraints.impl.PhizConstraintsChecker;
import gov.hhs.onc.phiz.crypto.ssl.revocation.impl.PhizRevocationChecker;
import gov.hhs.onc.phiz.crypto.utils.PhizCertificatePathUtils;
import gov.hhs.onc.phiz.crypto.utils.PhizCertificateUtils;
import gov.hhs.onc.phiz.logging.logstash.PhizLogstashTags;
import gov.hhs.onc.phiz.logging.logstash.impl.PhizLogstashMarkers;
import gov.hhs.onc.phiz.utils.PhizFunctionUtils;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class PhizTrustManager extends X509ExtendedTrustManager implements BeanFactoryAware, PhizSslManagerBean<PKIXBuilderParameters> {
    private final static Logger LOGGER = LoggerFactory.getLogger(PhizTrustManager.class);

    private BeanFactory beanFactory;
    private List<PKIXCertPathChecker> certPathCheckers;
    private X509CertSelector certSelector;
    private String constraintsCheckerBeanName;
    private KeyStore keyStore;
    private Provider prov;
    private String revocationCheckerBeanName;
    private String type;
    private PKIXBuilderParameters builderParams;

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        this.checkTrusted(PhizSslLocation.CLIENT, certs, authType, null, null, null);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType, @Nullable Socket socket) throws CertificateException {
        this.checkTrusted(PhizSslLocation.CLIENT, certs, authType, ((SSLSocket) socket), SSLSocket::isConnected, SSLSocket::getHandshakeSession);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType, @Nullable SSLEngine engine) throws CertificateException {
        this.checkTrusted(PhizSslLocation.CLIENT, certs, authType, engine, null, SSLEngine::getHandshakeSession);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        this.checkTrusted(PhizSslLocation.SERVER, certs, authType, null, null, null);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType, @Nullable Socket socket) throws CertificateException {
        this.checkTrusted(PhizSslLocation.SERVER, certs, authType, ((SSLSocket) socket), SSLSocket::isConnected, SSLSocket::getHandshakeSession);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType, @Nullable SSLEngine engine) throws CertificateException {
        this.checkTrusted(PhizSslLocation.SERVER, certs, authType, engine, null, SSLEngine::getHandshakeSession);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.builderParams.getTrustAnchors().stream().map(TrustAnchor::getTrustedCert).filter(Objects::nonNull).toArray(X509Certificate[]::new);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.builderParams = new PKIXBuilderParameters(this.keyStore, this.certSelector);
        this.builderParams.setRevocationEnabled(false);

        Optional.ofNullable(this.certPathCheckers).ifPresent(certPathCheckers -> certPathCheckers.stream().forEach(this.builderParams::addCertPathChecker));
    }

    private <T> void checkTrusted(PhizSslLocation loc, X509Certificate[] certs, String authType, @Nullable T component,
        @Nullable Function<T, Boolean> componentAvailableMapper, @Nullable Function<T, SSLSession> handshakeSessionMapper) throws CertificateException {
        String certSubjectDnNamesStr = null, certIssuerDnNamesStr = null, certSerialNumsStr = null;

        SslTrustEvent event = new SslTrustEventImpl();
        event.setLocation(loc);

        try {
            event.setAuthType(authType);

            boolean certsEmpty = ArrayUtils.isEmpty(certs);

            if (!certsEmpty) {
                event.setCertificates(PhizFunctionUtils.mapToStringArray(certs));

                certSubjectDnNamesStr = StringUtils.join(PhizCertificateUtils.buildSubjectDnNames(certs), ", ");
                certIssuerDnNamesStr = StringUtils.join(PhizCertificateUtils.buildIssuerDnNames(certs), ", ");
                certSerialNumsStr = StringUtils.join(PhizCertificateUtils.buildSerialNumbers(certs), ", ");
            }

            if (StringUtils.isEmpty(authType)) {
                throw new IllegalArgumentException(
                    String
                        .format(
                            "SSL %s certificate chain (subjectDnNames=[%s], issuerDnNames=[%s], serialNums=[%s]) trust checking authentication type must be specified.",
                            loc.getId(), certSubjectDnNamesStr, certIssuerDnNamesStr, certSerialNumsStr));
            }

            if (certsEmpty) {
                throw new IllegalArgumentException(String.format("SSL %s trust checking (authType=%s) certificate chain must be specified.", loc.name()
                    .toLowerCase(), authType));
            }

            if ((component != null) && ((componentAvailableMapper == null) || componentAvailableMapper.apply(component))) {
                // noinspection ConstantConditions
                SSLSession handshakeSession = handshakeSessionMapper.apply(component);

                if (handshakeSession == null) {
                    throw new CertificateException(
                        String
                            .format(
                                "Unable to get SSL %s handshake session from component (class=%s) during certificate chain (subjectDnNames=[%s], issuerDnNames=[%s], serialNums=[%s]) trust checking (authType=%s).",
                                loc.getId(), component.getClass().getName(), certSubjectDnNamesStr, certIssuerDnNamesStr, certSerialNumsStr, authType));
                }
            }

            try {
                X509CertSelector certSelector = new X509CertSelector();
                certSelector.setCertificate(certs[0]);

                PKIXBuilderParameters certBuilderParams = ((PKIXBuilderParameters) this.builderParams.clone());
                certBuilderParams.setTargetCertConstraints(certSelector);
                certBuilderParams.addCertStore(PhizCertificatePathUtils.buildStore(certs));

                X509Certificate issuerCert = PhizCertificatePathUtils.findRootCertificate(certs[0], certBuilderParams);

                if(issuerCert == null) {
                    LOGGER
                            .info(
                                    PhizLogstashMarkers.append(PhizLogstashTags.SSL, event),
                                    String
                                            .format(
                                                    "Unable to get SSL %s root certificate from component (class=%s) during certificate chain (subjectDnNames=[%s], issuerDnNames=[%s], serialNums=[%s]) trust checking (authType=%s).",
                                                    loc.getId(), component.getClass().getName(), certSubjectDnNamesStr, certIssuerDnNamesStr, certSerialNumsStr, authType));

                    throw new CertificateException(String
                            .format(
                                    "Unable to get SSL %s root certificate from component (class=%s) during certificate chain (subjectDnNames=[%s], issuerDnNames=[%s], serialNums=[%s]) trust checking (authType=%s).",
                                    loc.getId(), component.getClass().getName(), certSubjectDnNamesStr, certIssuerDnNamesStr, certSerialNumsStr, authType));
                }

                certBuilderParams.addCertPathChecker(((PhizConstraintsChecker) this.beanFactory.getBean(this.constraintsCheckerBeanName, loc, issuerCert)));

                certBuilderParams.addCertPathChecker(((PhizRevocationChecker) this.beanFactory.getBean(this.revocationCheckerBeanName, loc, issuerCert)));

                CertPathBuilder builder = CertPathBuilder.getInstance(this.type, this.prov);

                PKIXCertPathBuilderResult builderResult = ((PKIXCertPathBuilderResult) builder.build(certBuilderParams));
                X509Certificate[] pathCerts =
                    builderResult.getCertPath().getCertificates().stream().map(cert -> ((X509Certificate) cert)).toArray(X509Certificate[]::new);
                X509Certificate trustAnchorCert = builderResult.getTrustAnchor().getTrustedCert();

                event.setPathCertificates(PhizFunctionUtils.mapToStringArray(pathCerts));
                event.setTrustAnchorCertificate(trustAnchorCert.toString());
                event.setTrusted(true);

                LOGGER
                    .info(
                        PhizLogstashMarkers.append(PhizLogstashTags.SSL, event),
                        String
                            .format(
                                "SSL %s certificate chain (subjectDnNames=[%s], issuerDnNames=[%s], serialNums=[%s]) is trusted (pathSubjectDnNames=[%s], pathIssuerDnNames=[%s], pathSerialNums=[%s], trustAnchorSubjectDnName=%s, trustAnchorIssuerDnName=%s, trustAnchorSerialNum=%s).",
                                loc.getId(), certSubjectDnNamesStr, certIssuerDnNamesStr, certSerialNumsStr,
                                StringUtils.join(PhizCertificateUtils.buildSubjectDnNames(pathCerts), ", "),
                                StringUtils.join(PhizCertificateUtils.buildIssuerDnNames(pathCerts), ", "),
                                StringUtils.join(PhizCertificateUtils.buildSerialNumbers(pathCerts), ", "),
                                trustAnchorCert.getSubjectX500Principal().getName(), trustAnchorCert.getIssuerX500Principal().getName(),
                                trustAnchorCert.getSerialNumber()));
            } catch (Exception e) {
                throw new CertificateException(String.format(
                    "Unable to build SSL %s certificate chain (subjectDnNames=[%s], issuerDnNames=[%s], serialNums=[%s]) for trust checking (authType=%s).",
                    loc.getId(), certSubjectDnNamesStr, certIssuerDnNamesStr, certSerialNumsStr, authType), e);
            }
        } catch (Exception e) {
            LOGGER.error(PhizLogstashMarkers.append(PhizLogstashTags.SSL, event), String.format(
                "SSL %s certificate chain (subjectDnNames=[%s], issuerDnNames=[%s], serialNums=[%s]) is not trusted.", loc.getId(), certSubjectDnNamesStr,
                certIssuerDnNamesStr, certSerialNumsStr), e);

            throw e;
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public PKIXBuilderParameters getBuilderParameters() {
        return this.builderParams;
    }

    @Nullable
    public List<PKIXCertPathChecker> getCertificatePathCheckers() {
        return this.certPathCheckers;
    }

    public void setCertificatePathCheckers(@Nullable List<PKIXCertPathChecker> certPathCheckers) {
        this.certPathCheckers = certPathCheckers;
    }

    public X509CertSelector getCertificateSelector() {
        return this.certSelector;
    }

    public void setCertificateSelector(X509CertSelector certSelector) {
        this.certSelector = certSelector;
    }

    public String getConstraintsCheckerBeanName() {
        return this.constraintsCheckerBeanName;
    }

    public void setConstraintsCheckerBeanName(String constraintsCheckerBeanName) {
        this.constraintsCheckerBeanName = constraintsCheckerBeanName;
    }

    @Override
    public KeyStore getKeyStore() {
        return this.keyStore;
    }

    @Override
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    @Override
    public Provider getProvider() {
        return this.prov;
    }

    @Override
    public void setProvider(Provider prov) {
        this.prov = prov;
    }

    public String getRevocationCheckerBeanName() {
        return this.revocationCheckerBeanName;
    }

    public void setRevocationCheckerBeanName(String revocationCheckerBeanName) {
        this.revocationCheckerBeanName = revocationCheckerBeanName;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
