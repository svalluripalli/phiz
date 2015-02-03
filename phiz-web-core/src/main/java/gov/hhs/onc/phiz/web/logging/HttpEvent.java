package gov.hhs.onc.phiz.web.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.springframework.http.HttpHeaders;

public interface HttpEvent {
    @JsonProperty
    @Nullable
    public Long getContentLength();

    public void setContentLength(@Nullable Long contentLen);

    @JsonProperty
    @Nullable
    public String getContentType();

    public void setContentType(@Nullable String contentType);

    @JsonProperty
    public HttpHeaders getHeaders();
}
