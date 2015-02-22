package gov.hhs.onc.phiz.crypto.ssl.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.hhs.onc.phiz.crypto.ssl.PhizSslLocation;

public interface SslTrustEvent {
    @JsonProperty
    public String getAuthType();

    public void setAuthType(String authType);

    @JsonProperty
    public String[] getCertificates();

    public void setCertificates(String[] certs);

    @JsonProperty
    public PhizSslLocation getLocation();

    @JsonProperty
    public boolean isTrusted();

    public void setTrusted(boolean trusted);
}
