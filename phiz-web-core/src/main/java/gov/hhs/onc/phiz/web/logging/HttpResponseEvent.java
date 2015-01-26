package gov.hhs.onc.phiz.web.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.hhs.onc.phiz.logging.MarkerFieldName;
import javax.servlet.http.HttpServletResponse;

@MarkerFieldName("httpResponse")
public interface HttpResponseEvent extends HttpEvent<HttpServletResponse> {
    @JsonProperty
    public int getStatus();
}
