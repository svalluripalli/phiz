package gov.hhs.onc.phiz.crypto;

import br.net.woodstock.rockframework.security.cert.CertificateRequest;
import br.net.woodstock.rockframework.security.cert.CertificateResponse;

public interface PhizCredential {
    public boolean isIssued();

    public boolean isSelfIssued();

    public CertificateRequest getCertificateRequest();

    public void setCertificateRequest(CertificateRequest certReq);

    public CertificateResponse getCertificateResponse();

    public void setCertificateResponse(CertificateResponse certResp);

    public PhizCredential getIssuerCredential();

    public void setIssuerCredential(PhizCredential issuerCred);
}
