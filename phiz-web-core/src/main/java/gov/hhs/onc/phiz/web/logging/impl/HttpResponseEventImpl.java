package gov.hhs.onc.phiz.web.logging.impl;

import gov.hhs.onc.phiz.web.logging.HttpResponseEvent;
import org.springframework.http.HttpStatus;

public class HttpResponseEventImpl extends AbstractHttpEvent implements HttpResponseEvent {
    private HttpStatus status;

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
