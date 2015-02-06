package gov.hhs.onc.phiz.web.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.hhs.onc.phiz.logging.logstash.MarkerObjectFieldName;

@MarkerObjectFieldName("httpResponse")
public interface HttpResponseEvent extends HttpEvent {
    @JsonProperty
    public Integer getStatus();

    public void setStatus(Integer status);

    @JsonProperty
    public String getStatusMessage();

    public void setStatusMessage(String statusMsg);
}
