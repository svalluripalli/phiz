package gov.hhs.onc.phiz.crypto.ssl.logging.impl;

import gov.hhs.onc.phiz.crypto.ssl.PhizSslLocation;
import gov.hhs.onc.phiz.crypto.ssl.logging.SslClientTrustEvent;

public class SslClientTrustEventImpl extends AbstractSslTrustEvent implements SslClientTrustEvent {
    public SslClientTrustEventImpl() {
        super(PhizSslLocation.CLIENT);
    }
}
