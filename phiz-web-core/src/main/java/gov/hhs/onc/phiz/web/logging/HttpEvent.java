package gov.hhs.onc.phiz.web.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface HttpEvent<T> {
    @JsonProperty("headers")
    public Map<String, List<String>> getHeaderMap();

    public Set<String> getHeaderNames();

    public List<String> getHeaders(String headerName);

    @JsonProperty
    public String getContentType();

    public T getDescriptor();
}
