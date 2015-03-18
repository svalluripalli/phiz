package gov.hhs.onc.phiz.crypto.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.hhs.onc.phiz.crypto.ssl.PhizSslLocation;

public interface SslEvent {
    @JsonProperty
    public PhizSslLocation getLocation();

    public void setLocation(PhizSslLocation loc);
}
