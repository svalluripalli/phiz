package gov.hhs.onc.phiz.crypto.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.hhs.onc.phiz.logging.logstash.MarkerObjectFieldName;
import javax.annotation.Nullable;

@MarkerObjectFieldName("sslTrust")
public interface SslTrustEvent extends SslEvent {
    @JsonProperty
    @Nullable
    public String getAuthType();

    public void setAuthType(@Nullable String authType);

    @JsonProperty
    public String[] getCertificates();

    public void setCertificates(String[] certs);

    @JsonProperty
    @Nullable
    public String[] getPathCertificates();

    public void setPathCertificates(@Nullable String[] pathCerts);

    @JsonProperty
    @Nullable
    public String getTrustAnchorCertificate();

    public void setTrustAnchorCertificate(@Nullable String trustAnchorCert);

    @JsonProperty
    public boolean isTrusted();

    public void setTrusted(boolean trusted);
}
