package gov.hhs.onc.phiz.web.listener.impl;

import ch.qos.logback.classic.ClassicConstants;
import java.util.Objects;
import java.util.stream.Stream;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;

public class MdcServletRequestListener extends AbstractPhizServletRequestListener {
    private final static String X_FORWARDED_FOR_HTTP_HEADER_NAME = "X-Forwarded-For";

    private final static String[] REQ_MDC_KEYS = ArrayUtils.toArray(ClassicConstants.REQUEST_QUERY_STRING, ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY,
        ClassicConstants.REQUEST_REQUEST_URI, ClassicConstants.REQUEST_REQUEST_URL, ClassicConstants.REQUEST_USER_AGENT_MDC_KEY,
        ClassicConstants.REQUEST_X_FORWARDED_FOR);

    @Override
    public void requestDestroyed(ServletRequestEvent servletReqEvent) {
        Stream.of(REQ_MDC_KEYS).forEach(MDC::remove);
    }

    @Override
    public void requestInitialized(ServletRequestEvent servletReqEvent) {
        HttpServletRequest servletReq = ((HttpServletRequest) servletReqEvent.getServletRequest());

        MDC.put(ClassicConstants.REQUEST_QUERY_STRING, servletReq.getQueryString());
        MDC.put(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY, servletReq.getRemoteHost());
        MDC.put(ClassicConstants.REQUEST_REQUEST_URI, servletReq.getRequestURI());
        MDC.put(ClassicConstants.REQUEST_REQUEST_URI, Objects.toString(servletReq.getRequestURL(), null));
        MDC.put(ClassicConstants.REQUEST_USER_AGENT_MDC_KEY, servletReq.getHeader(HttpHeaders.USER_AGENT));
        MDC.put(ClassicConstants.REQUEST_X_FORWARDED_FOR, servletReq.getHeader(X_FORWARDED_FOR_HTTP_HEADER_NAME));
    }
}
