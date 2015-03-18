package gov.hhs.onc.phiz.crypto.logging.impl;

import gov.hhs.onc.phiz.crypto.logging.SslHelloEvent;

public class SslHelloEventImpl extends AbstractSslEvent implements SslHelloEvent {
    private String[] cipherSuites;
    private String protocol;

    @Override
    public String[] getCipherSuites() {
        return this.cipherSuites;
    }

    @Override
    public void setCipherSuites(String[] cipherSuites) {
        this.cipherSuites = cipherSuites;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
