package gov.hhs.onc.phiz.web.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.hhs.onc.phiz.logging.logstash.MarkerObjectFieldName;
import javax.servlet.http.HttpServletResponse;

@MarkerObjectFieldName("httpResponse")
public interface HttpResponseEvent extends HttpEvent<HttpServletResponse> {
    @JsonProperty
    public int getStatus();

    @JsonProperty
    public String getStatusMessage();
}
