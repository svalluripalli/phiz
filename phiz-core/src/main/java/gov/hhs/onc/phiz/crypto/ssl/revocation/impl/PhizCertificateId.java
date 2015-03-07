package gov.hhs.onc.phiz.crypto.ssl.revocation.impl;

import gov.hhs.onc.phiz.crypto.utils.PhizCryptoUtils;
import java.math.BigInteger;
import java.util.Comparator;
import javax.annotation.Nullable;
import org.bouncycastle.asn1.ocsp.CertID;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.operator.DigestCalculator;

public class PhizCertificateId extends CertificateID implements Comparable<PhizCertificateId> {
    private X509CertificateHolder issuerCertHolder;

    public PhizCertificateId(DigestCalculator digestCalc, X509CertificateHolder issuerCertHolder, BigInteger certSerialNum) throws OCSPException {
        super(digestCalc, issuerCertHolder, certSerialNum);

        this.issuerCertHolder = issuerCertHolder;
    }

    public PhizCertificateId(CertificateID certId) {
        this(certId.toASN1Object());
    }

    public PhizCertificateId(CertID certId) {
        super(certId);
    }

    public boolean matches(PhizCertificateId certId) throws OCSPException {
        return this.matches(certId.getIssuerCertificateHolder(), certId.getSerialNumber());
    }

    public boolean matches(X509CertificateHolder issuerCertHolder, BigInteger certSerialNum) throws OCSPException {
        return (this.matchesIssuer(issuerCertHolder, PhizCryptoUtils.DIGEST_CALC_PROV) && this.getSerialNumber().equals(certSerialNum));
    }

    @Override
    public int compareTo(PhizCertificateId obj) {
        return Comparator.comparing(Object::hashCode).compare(this, obj);
    }

    @Nullable
    public X509CertificateHolder getIssuerCertificateHolder() {
        return this.issuerCertHolder;
    }
}
