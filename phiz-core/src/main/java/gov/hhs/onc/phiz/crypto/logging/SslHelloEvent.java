package gov.hhs.onc.phiz.crypto.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.hhs.onc.phiz.logging.logstash.MarkerObjectFieldName;

@MarkerObjectFieldName("sslHello")
public interface SslHelloEvent extends SslEvent {
    @JsonProperty
    public String[] getCipherSuites();

    public void setCipherSuites(String[] cipherSuites);

    @JsonProperty
    public String getProtocol();

    public void setProtocol(String protocol);
}
