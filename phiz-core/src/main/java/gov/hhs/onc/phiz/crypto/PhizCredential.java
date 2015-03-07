package gov.hhs.onc.phiz.crypto;

import br.net.woodstock.rockframework.security.Identity;
import br.net.woodstock.rockframework.security.cert.CertificateRequest;
import br.net.woodstock.rockframework.security.cert.CertificateResponse;
import gov.hhs.onc.phiz.crypto.ssl.revocation.OcspRevokeReasonType;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.annotation.Nullable;
import org.bouncycastle.cert.ocsp.CertificateStatus;

public interface PhizCredential {
    public boolean isRevoked();

    public boolean isIssued();

    public boolean isRootIssuer();

    public boolean isIssuer();

    @Nullable
    public X509Certificate getCertificate();

    public CertificateRequest getCertificateRequest();

    public void setCertificateRequest(CertificateRequest certReq);

    @Nullable
    public CertificateResponse getCertificateResponse();

    public void setCertificateResponse(@Nullable CertificateResponse certResp);

    @Nullable
    public X509Certificate[] getCertificates();

    @Nullable
    public CertificateStatus getCertificateStatus();

    @Nullable
    public Identity getIdentity();

    @Nullable
    public PhizCredential getIssuerCredential();

    public void setIssuerCredential(@Nullable PhizCredential issuerCred);

    @Nullable
    public PrivateKey getPrivateKey();

    @Nullable
    public OcspRevokeReasonType getRevocationReason();

    public void setRevocationReason(@Nullable OcspRevokeReasonType revocationReason);

    @Nullable
    public Date getRevocationTime();

    public void setRevocationTime(@Nullable Date revocationTime);
}
