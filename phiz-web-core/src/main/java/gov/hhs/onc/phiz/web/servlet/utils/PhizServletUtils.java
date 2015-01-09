package gov.hhs.onc.phiz.web.servlet.utils;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.EnumerationUtils;
import org.springframework.http.HttpHeaders;

public final class PhizServletUtils {
    private PhizServletUtils() {
    }

    public static HttpHeaders getHeaders(HttpServletResponse servletResp) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(servletResp.getHeaderNames().stream()
            .collect(Collectors.toMap(Function.<String> identity(), (String headerName) -> new ArrayList<>(servletResp.getHeaders(headerName)))));

        return headers;
    }

    public static HttpHeaders getHeaders(HttpServletRequest servletReq) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(EnumerationUtils.toList(servletReq.getHeaderNames()).stream()
            .collect(Collectors.toMap(Function.<String> identity(), (headerName) -> EnumerationUtils.toList(servletReq.getHeaders(headerName)))));

        return headers;
    }
}
