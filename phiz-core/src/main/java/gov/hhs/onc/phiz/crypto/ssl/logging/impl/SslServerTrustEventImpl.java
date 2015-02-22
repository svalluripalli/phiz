package gov.hhs.onc.phiz.crypto.ssl.logging.impl;

import gov.hhs.onc.phiz.crypto.ssl.PhizSslLocation;
import gov.hhs.onc.phiz.crypto.ssl.logging.SslServerTrustEvent;

public class SslServerTrustEventImpl extends AbstractSslTrustEvent implements SslServerTrustEvent {
    public SslServerTrustEventImpl() {
        super(PhizSslLocation.SERVER);
    }
}
