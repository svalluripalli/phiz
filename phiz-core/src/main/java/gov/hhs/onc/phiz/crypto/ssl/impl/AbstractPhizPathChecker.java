package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.crypto.ssl.PhizSslLocation;
import java.math.BigInteger;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertPathValidatorException.Reason;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractPhizPathChecker extends PKIXCertPathChecker implements InitializingBean {
    protected final static Map<Class<? extends CertificateException>, Reason> CAUSE_REASON_MAP = Arrays
        .<Entry<Class<? extends CertificateException>, Reason>> asList(new ImmutablePair<>(CertificateExpiredException.class, BasicReason.EXPIRED),
            new ImmutablePair<>(CertificateNotYetValidException.class, BasicReason.NOT_YET_VALID),
            new ImmutablePair<>(CertificateRevokedException.class, BasicReason.REVOKED)).stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    @Resource(name = "dateFormatUtcDisplay")
    protected FastDateFormat displayDateFormat;

    protected PhizSslLocation loc;
    protected X509Certificate issuerCert;
    protected Reason defaultReason;
    protected X509CertificateHolder issuerCertHolder;

    protected AbstractPhizPathChecker(PhizSslLocation loc, X509Certificate issuerCert, Reason defaultReason) {
        this.loc = loc;
        this.issuerCert = issuerCert;
        this.defaultReason = defaultReason;
    }

    @Override
    public void check(Certificate baseCert, Collection<String> unresolvedCriticalExts) throws CertPathValidatorException {
        X509Certificate cert = ((X509Certificate) baseCert);

        this.checkInternal(cert, cert.getSubjectX500Principal().getName(), cert.getIssuerX500Principal().getName(), cert.getSerialNumber());
    }

    @Override
    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("Forward certificate path checking is not supported.");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.issuerCertHolder = new JcaX509CertificateHolder(this.issuerCert);
    }

    protected abstract void checkInternal(X509Certificate cert, String certSubjectDnNameStr, String certIssuerDnNameStr, BigInteger certSerialNum)
        throws CertPathValidatorException;

    protected CertPathValidatorException buildException(String msg) {
        return this.buildException(msg, null);
    }

    protected CertPathValidatorException buildException(String msg, @Nullable Throwable cause) {
        Reason reason = this.defaultReason;

        if (cause != null) {
            final Class<? extends Throwable> causeClass = cause.getClass();

            reason =
                CAUSE_REASON_MAP.entrySet().stream().filter(causeEntry -> causeEntry.getKey().isAssignableFrom(causeClass)).findFirst().map(Entry::getValue)
                    .orElse(this.defaultReason);
        }

        return new CertPathValidatorException(msg, cause, null, -1, reason);
    }

    @Override
    public boolean isForwardCheckingSupported() {
        return false;
    }

    @Nullable
    @Override
    public Set<String> getSupportedExtensions() {
        return null;
    }
}
