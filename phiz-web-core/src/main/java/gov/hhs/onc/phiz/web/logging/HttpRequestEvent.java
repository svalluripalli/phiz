package gov.hhs.onc.phiz.web.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.hhs.onc.phiz.logging.logstash.MarkerObjectFieldName;
import javax.annotation.Nullable;
import org.springframework.http.HttpMethod;

@MarkerObjectFieldName("httpRequest")
public interface HttpRequestEvent extends HttpEvent {
    @JsonProperty
    @Nullable
    public String getAuthType();

    public void setAuthType(@Nullable String authType);

    @JsonProperty
    @Nullable
    public String getContextPath();

    public void setContextPath(@Nullable String contextPath);

    @JsonProperty
    @Nullable
    public String getLocalName();

    public void setLocalName(@Nullable String localName);

    @JsonProperty
    @Nullable
    public Integer getLocalPort();

    public void setLocalPort(@Nullable Integer localPort);

    @JsonProperty
    public HttpMethod getMethod();

    public void setMethod(HttpMethod method);

    @JsonProperty
    public String getPathInfo();

    public void setPathInfo(String pathInfo);

    @JsonProperty
    public String getProtocol();

    public void setProtocol(String protocol);

    @JsonProperty
    public String getQueryString();

    public void setQueryString(String queryStr);

    @JsonProperty
    @Nullable
    public String getRemoteAddr();

    public void setRemoteAddr(@Nullable String remoteAddr);

    @JsonProperty
    public String getRemoteHost();

    public void setRemoteHost(String remoteHost);

    @JsonProperty
    public Integer getRemotePort();

    public void setRemotePort(Integer remotePort);

    @JsonProperty
    public String getScheme();

    public void setScheme(String scheme);

    @JsonProperty
    @Nullable
    public String getServerName();

    public void setServerName(@Nullable String serverName);

    @JsonProperty
    @Nullable
    public Integer getServerPort();

    public void setServerPort(@Nullable Integer serverPort);

    @JsonProperty
    @Nullable
    public String getServletPath();

    public void setServletPath(@Nullable String servletPath);

    @JsonProperty
    public String getUrl();

    public void setUrl(String url);

    @JsonProperty
    @Nullable
    public String getUserPrincipal();

    public void setUserPrincipal(@Nullable String userPrincipal);
}
