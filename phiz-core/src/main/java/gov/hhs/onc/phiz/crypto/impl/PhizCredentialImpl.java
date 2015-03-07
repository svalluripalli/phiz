package gov.hhs.onc.phiz.crypto.impl;

import br.net.woodstock.rockframework.security.Identity;
import br.net.woodstock.rockframework.security.cert.CertificateRequest;
import br.net.woodstock.rockframework.security.cert.CertificateResponse;
import gov.hhs.onc.phiz.crypto.PhizCredential;
import gov.hhs.onc.phiz.crypto.ssl.revocation.OcspRevokeReasonType;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.annotation.Nullable;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.RevokedStatus;

public class PhizCredentialImpl implements PhizCredential {
    private CertificateRequest certReq;
    private CertificateResponse certResp;
    private PhizCredential issuerCred;
    private OcspRevokeReasonType revocationReason;
    private Date revocationTime;

    @Override
    public boolean isRevoked() {
        return (this.revocationReason != null);
    }

    @Override
    public boolean isIssued() {
        return (this.certResp != null);
    }

    @Override
    public boolean isRootIssuer() {
        return (this.isIssuer() && (this.issuerCred == null));
    }

    @Override
    public boolean isIssuer() {
        return this.certReq.isBasicConstraintsCritical();
    }

    @Nullable
    @Override
    public X509Certificate getCertificate() {
        Identity identity = this.getIdentity();

        return ((identity != null) ? ((X509Certificate) identity.getChain().get(0)) : null);
    }

    @Override
    public CertificateRequest getCertificateRequest() {
        return this.certReq;
    }

    @Override
    public void setCertificateRequest(CertificateRequest certReq) {
        this.certReq = certReq;
    }

    @Nullable
    @Override
    public CertificateResponse getCertificateResponse() {
        return this.certResp;
    }

    @Override
    public void setCertificateResponse(@Nullable CertificateResponse certResp) {
        this.certResp = certResp;
    }

    @Nullable
    @Override
    public X509Certificate[] getCertificates() {
        Identity identity = this.getIdentity();

        return ((identity != null) ? identity.getChain().stream().map(cert -> ((X509Certificate) cert)).toArray(X509Certificate[]::new) : null);
    }

    @Nullable
    @Override
    public CertificateStatus getCertificateStatus() {
        if (!this.isRevoked()) {
            return CertificateStatus.GOOD;
        }

        if (this.revocationTime == null) {
            this.revocationTime = new Date();
        }

        return new RevokedStatus(this.revocationTime, this.revocationReason.getTag());
    }

    @Nullable
    @Override
    public Identity getIdentity() {
        return (this.isIssued() ? this.certResp.getIdentity() : null);
    }

    @Nullable
    @Override
    public PhizCredential getIssuerCredential() {
        return this.issuerCred;
    }

    @Override
    public void setIssuerCredential(@Nullable PhizCredential issuerCred) {
        this.issuerCred = issuerCred;
    }

    @Nullable
    @Override
    public PrivateKey getPrivateKey() {
        Identity identity = this.getIdentity();

        return ((identity != null) ? identity.getPrivateKey() : null);
    }

    @Nullable
    @Override
    public OcspRevokeReasonType getRevocationReason() {
        return this.revocationReason;
    }

    @Override
    public void setRevocationReason(@Nullable OcspRevokeReasonType revocationReason) {
        this.revocationReason = revocationReason;
    }

    @Nullable
    @Override
    public Date getRevocationTime() {
        return this.revocationTime;
    }

    @Override
    public void setRevocationTime(@Nullable Date revocationTime) {
        this.revocationTime = revocationTime;
    }
}
