package gov.hhs.onc.phiz.web.logging.impl;

import gov.hhs.onc.phiz.web.logging.HttpResponseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletResponse;

public class HttpResponseEventImpl extends AbstractHttpEvent<HttpServletResponse> implements HttpResponseEvent {
    public HttpResponseEventImpl(HttpServletResponse desc) {
        super(desc);
    }

    @Override
    public Set<String> getHeaderNames() {
        Set<String> headerNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        headerNames.addAll(this.desc.getHeaderNames());

        return headerNames;
    }

    @Override
    public List<String> getHeaders(String headerName) {
        return new ArrayList<>(this.desc.getHeaders(headerName));
    }

    @Override
    public String getContentType() {
        return this.desc.getContentType();
    }

    @Override
    public int getStatus() {
        return this.desc.getStatus();
    }
}
