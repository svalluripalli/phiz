package gov.hhs.onc.phiz.web.logging.impl;

import gov.hhs.onc.phiz.web.logging.HttpEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractHttpEvent<T> implements HttpEvent<T> {
    protected T desc;

    protected AbstractHttpEvent(T desc) {
        this.desc = desc;
    }

    @Override
    public Map<String, List<String>> getHeaderMap() {
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        this.getHeaderNames().stream().forEach((headerName) -> headers.put(headerName.toLowerCase(), new ArrayList<>(this.getHeaders(headerName))));

        return headers;
    }

    @Override
    public T getDescriptor() {
        return this.desc;
    }
}
