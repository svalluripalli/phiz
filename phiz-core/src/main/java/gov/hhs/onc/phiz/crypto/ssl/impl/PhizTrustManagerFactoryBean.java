package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils;
import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils.PhizMethodAdvisor;
import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils.PhizMethodInterceptor;
import gov.hhs.onc.phiz.crypto.ssl.logging.SslTrustEvent;
import gov.hhs.onc.phiz.crypto.ssl.logging.impl.SslClientTrustEventImpl;
import gov.hhs.onc.phiz.crypto.ssl.logging.impl.SslServerTrustEventImpl;
import gov.hhs.onc.phiz.logging.logstash.PhizLogstashTags;
import gov.hhs.onc.phiz.logging.logstash.impl.PhizLogstashMarkers;
import java.security.cert.CertSelector;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhizTrustManagerFactoryBean extends AbstractPhizSslManagerFactoryBean<TrustManager, CertPathTrustManagerParameters> {
    private final static String CHECK_CLIENT_TRUSTED_METHOD_NAME = "checkClientTrusted";
    private final static String CHECK_SERVER_TRUSTED_METHOD_NAME = "checkServerTrusted";

    private final static Logger LOGGER = LoggerFactory.getLogger(PhizTrustManagerFactoryBean.class);

    private CertSelector certSelector;

    public PhizTrustManagerFactoryBean() {
        super(TrustManager.class);
    }

    @Override
    public TrustManager getObject() throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(this.type, this.prov);
        factory.init(this.buildFactoryParameters());

        TrustManager manager = factory.getTrustManagers()[0];

        return PhizProxyUtils.buildProxyFactory(
            manager,
            X509ExtendedTrustManager.class,
            new PhizMethodAdvisor(
                ((PhizMethodInterceptor) (invocation, method, methodName, args, target) -> {
                    X509Certificate[] certs = ((X509Certificate[]) args[0]);
                    String[] certSubjectDns = Stream.of(certs).map(cert -> cert.getSubjectDN().getName()).toArray(String[]::new);

                    SslTrustEvent event =
                        (invocation.getMethod().getName().equals(CHECK_CLIENT_TRUSTED_METHOD_NAME)
                            ? new SslClientTrustEventImpl() : new SslServerTrustEventImpl());
                    event.setAuthType(((String) args[1]));
                    event.setCertificates(Stream.of(certs).map(Object::toString).toArray(String[]::new));

                    try {
                        invocation.proceed();

                        event.setTrusted(true);

                        LOGGER.debug(
                            PhizLogstashMarkers.append(PhizLogstashTags.SSL, event),
                            String.format("SSL %s certificate chain (subjects=[%s]) is trusted.", event.getLocation().name().toLowerCase(),
                                StringUtils.join(certSubjectDns, "; ")));

                        return null;
                    } catch (CertificateException e) {
                        LOGGER.error(
                            PhizLogstashMarkers.append(PhizLogstashTags.SSL, event),
                            String.format("SSL %s certificate chain (subjects=[%s]) is not trusted.", event.getLocation().name().toLowerCase(),
                                StringUtils.join(certSubjectDns, "; ")), e);

                        throw e;
                    }
                }), CHECK_CLIENT_TRUSTED_METHOD_NAME, CHECK_SERVER_TRUSTED_METHOD_NAME)).getProxy();
    }

    @Override
    protected CertPathTrustManagerParameters buildFactoryParameters() throws Exception {
        PKIXBuilderParameters builderParams = new PKIXBuilderParameters(this.keyStore, this.certSelector);
        builderParams.setRevocationEnabled(false);

        return new CertPathTrustManagerParameters(builderParams);
    }

    public CertSelector getCertSelector() {
        return this.certSelector;
    }

    public void setCertSelector(CertSelector certSelector) {
        this.certSelector = certSelector;
    }
}
