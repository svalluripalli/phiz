package gov.hhs.onc.phiz.crypto.logging.impl;

import gov.hhs.onc.phiz.crypto.logging.SslTrustEvent;
import javax.annotation.Nullable;

public class SslTrustEventImpl extends AbstractSslEvent implements SslTrustEvent {
    private String authType;
    private String[] certs;
    private String[] pathCerts;
    private String trustAnchorCert;
    private boolean trusted;

    @Nullable
    @Override
    public String getAuthType() {
        return this.authType;
    }

    @Override
    public void setAuthType(@Nullable String authType) {
        this.authType = authType;
    }

    @Override
    public String[] getCertificates() {
        return this.certs;
    }

    @Override
    public void setCertificates(String[] certs) {
        this.certs = certs;
    }

    @Nullable
    @Override
    public String[] getPathCertificates() {
        return this.pathCerts;
    }

    @Override
    public void setPathCertificates(@Nullable String[] pathCerts) {
        this.pathCerts = pathCerts;
    }

    @Nullable
    @Override
    public String getTrustAnchorCertificate() {
        return this.trustAnchorCert;
    }

    @Override
    public void setTrustAnchorCertificate(@Nullable String trustAnchorCert) {
        this.trustAnchorCert = trustAnchorCert;
    }

    @Override
    public boolean isTrusted() {
        return this.trusted;
    }

    @Override
    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }
}
