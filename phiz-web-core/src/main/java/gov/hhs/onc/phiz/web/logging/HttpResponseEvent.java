package gov.hhs.onc.phiz.web.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.hhs.onc.phiz.logging.logstash.MarkerObjectFieldName;
import org.springframework.http.HttpStatus;

@MarkerObjectFieldName("httpResponse")
public interface HttpResponseEvent extends HttpEvent {
    @JsonProperty
    public HttpStatus getStatus();
    
    public void setStatus(HttpStatus status);
}
