package gov.hhs.onc.phiz.web.servlet.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public final class PhizServletUtils {
    private PhizServletUtils() {
    }

    public static HttpServletResponse unwrapResponse(HttpServletResponse servletResp) {
        return ((servletResp instanceof HttpServletResponseWrapper) ? unwrapResponse(((HttpServletResponse) ((HttpServletResponseWrapper) servletResp)
            .getResponse())) : servletResp);
    }

    public static HttpServletRequest unwrapRequest(HttpServletRequest servletReq) {
        return ((servletReq instanceof HttpServletRequestWrapper)
            ? unwrapRequest(((HttpServletRequest) ((HttpServletRequestWrapper) servletReq).getRequest())) : servletReq);
    }
}
