package gov.hhs.onc.phiz.web.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.hhs.onc.phiz.logging.logstash.MarkerObjectFieldName;
import javax.servlet.http.HttpServletRequest;

@MarkerObjectFieldName("httpRequest")
public interface HttpRequestEvent extends HttpEvent<HttpServletRequest> {
    @JsonProperty
    public String getAuthType();

    @JsonProperty
    public long getContentLength();

    @JsonProperty
    public String getContextPath();

    @JsonProperty
    public String getLocalName();

    @JsonProperty
    public int getLocalPort();

    @JsonProperty
    public String getMethod();

    @JsonProperty
    public String getPathInfo();

    @JsonProperty
    public String getProtocol();

    @JsonProperty
    public String getQueryString();

    @JsonProperty
    public String getRemoteAddr();

    @JsonProperty
    public int getRemotePort();

    @JsonProperty
    public String getScheme();

    @JsonProperty
    public String getServerName();

    @JsonProperty
    public int getServerPort();

    @JsonProperty
    public String getServletPath();

    @JsonProperty
    public String getUrl();

    @JsonProperty
    public String getUserPrincipal();
}
