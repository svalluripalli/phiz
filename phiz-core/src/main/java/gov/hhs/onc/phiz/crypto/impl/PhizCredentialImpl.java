package gov.hhs.onc.phiz.crypto.impl;

import br.net.woodstock.rockframework.security.cert.CertificateRequest;
import br.net.woodstock.rockframework.security.cert.CertificateResponse;
import gov.hhs.onc.phiz.crypto.PhizCredential;

public class PhizCredentialImpl implements PhizCredential {
    private CertificateRequest certReq;
    private CertificateResponse certResp;
    private PhizCredential issuerCred;

    @Override
    public boolean isIssued() {
        return (this.certResp != null);
    }

    @Override
    public boolean isSelfIssued() {
        return (this.issuerCred == null);
    }

    @Override
    public CertificateRequest getCertificateRequest() {
        return this.certReq;
    }

    @Override
    public void setCertificateRequest(CertificateRequest certReq) {
        this.certReq = certReq;
    }

    @Override
    public CertificateResponse getCertificateResponse() {
        return this.certResp;
    }

    @Override
    public void setCertificateResponse(CertificateResponse certResp) {
        this.certResp = certResp;
    }

    @Override
    public PhizCredential getIssuerCredential() {
        return this.issuerCred;
    }

    @Override
    public void setIssuerCredential(PhizCredential issuerCred) {
        this.issuerCred = issuerCred;
    }
}
