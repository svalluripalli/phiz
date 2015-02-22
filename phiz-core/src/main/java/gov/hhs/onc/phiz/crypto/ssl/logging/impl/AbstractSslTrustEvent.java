package gov.hhs.onc.phiz.crypto.ssl.logging.impl;

import gov.hhs.onc.phiz.crypto.ssl.PhizSslLocation;
import gov.hhs.onc.phiz.crypto.ssl.logging.SslTrustEvent;

public abstract class AbstractSslTrustEvent implements SslTrustEvent {
    protected String authType;
    protected String[] certs;
    protected PhizSslLocation loc;
    protected boolean trusted;

    protected AbstractSslTrustEvent(PhizSslLocation loc) {
        this.loc = loc;
    }

    @Override
    public String getAuthType() {
        return this.authType;
    }

    @Override
    public void setAuthType(String authType) {
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

    @Override
    public PhizSslLocation getLocation() {
        return this.loc;
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
