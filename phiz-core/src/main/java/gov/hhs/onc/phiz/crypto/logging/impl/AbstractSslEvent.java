package gov.hhs.onc.phiz.crypto.logging.impl;

import gov.hhs.onc.phiz.crypto.logging.SslEvent;
import gov.hhs.onc.phiz.crypto.ssl.PhizSslLocation;

public abstract class AbstractSslEvent implements SslEvent {
    protected PhizSslLocation loc;

    @Override
    public PhizSslLocation getLocation() {
        return this.loc;
    }

    @Override
    public void setLocation(PhizSslLocation loc) {
        this.loc = loc;
    }
}
