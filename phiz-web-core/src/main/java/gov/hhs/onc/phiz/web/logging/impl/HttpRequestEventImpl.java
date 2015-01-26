package gov.hhs.onc.phiz.web.logging.impl;

import gov.hhs.onc.phiz.web.logging.HttpRequestEvent;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.EnumerationUtils;

public class HttpRequestEventImpl extends AbstractHttpEvent<HttpServletRequest> implements HttpRequestEvent {
    public HttpRequestEventImpl(HttpServletRequest desc) {
        super(desc);
    }

    @Override
    public Set<String> getHeaderNames() {
        Set<String> headerNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        headerNames.addAll(EnumerationUtils.toList(this.desc.getHeaderNames()));

        return headerNames;
    }

    @Override
    public List<String> getHeaders(String headerName) {
        return EnumerationUtils.toList(this.desc.getHeaders(headerName));
    }

    @Override
    public String getAuthType() {
        return this.desc.getAuthType();
    }

    @Override
    public long getContentLength() {
        return this.desc.getContentLengthLong();
    }

    @Override
    public String getContentType() {
        return this.desc.getContentType();
    }

    @Override
    public String getContextPath() {
        return this.desc.getContextPath();
    }

    @Override
    public String getLocalName() {
        return this.desc.getLocalName();
    }

    @Override
    public int getLocalPort() {
        return this.desc.getLocalPort();
    }

    @Override
    public String getMethod() {
        return this.desc.getMethod();
    }

    @Override
    public String getPathInfo() {
        return this.desc.getPathInfo();
    }

    @Override
    public String getProtocol() {
        return this.desc.getProtocol();
    }

    @Override
    public String getQueryString() {
        return this.desc.getQueryString();
    }

    @Override
    public String getRemoteAddr() {
        return this.desc.getRemoteAddr();
    }

    @Override
    public int getRemotePort() {
        return this.desc.getRemotePort();
    }

    @Override
    public String getScheme() {
        return this.desc.getScheme();
    }

    @Override
    public String getServerName() {
        return this.desc.getServerName();
    }

    @Override
    public int getServerPort() {
        return this.desc.getServerPort();
    }

    @Override
    public String getServletPath() {
        return this.desc.getServletPath();
    }

    @Override
    public String getUrl() {
        return this.desc.getRequestURL().toString();
    }

    @Override
    public String getUserPrincipal() {
        return Objects.toString(this.desc.getUserPrincipal(), null);
    }
}
