package gov.hhs.onc.phiz.web.ws.logging.impl;

import gov.hhs.onc.phiz.web.ws.PhizWsMessageDirection;
import gov.hhs.onc.phiz.web.ws.PhizWsEndpointType;
import gov.hhs.onc.phiz.web.ws.logging.WsMessageEvent;
import java.util.Map;
import org.slf4j.Logger;

public abstract class AbstractWsMessageEvent implements WsMessageEvent {
    protected PhizWsMessageDirection direction;
    protected String endpointAddr;
    protected PhizWsEndpointType endpointType;
    protected long eventId;
    protected Logger logger;
    protected Map<String, Object> soapFault;
    protected Map<String, Object> soapHeaders;
    protected String payload;

    @Override
    public PhizWsMessageDirection getDirection() {
        return this.direction;
    }

    @Override
    public void setDirection(PhizWsMessageDirection direction) {
        this.direction = direction;
    }

    @Override
    public String getEndpointAddress() {
        return this.endpointAddr;
    }

    @Override
    public void setEndpointAddress(String endpointAddr) {
        this.endpointAddr = endpointAddr;
    }

    @Override
    public PhizWsEndpointType getEndpointType() {
        return this.endpointType;
    }

    @Override
    public void setEndpointType(PhizWsEndpointType endpointType) {
        this.endpointType = endpointType;
    }

    @Override
    public long getEventId() {
        return this.eventId;
    }

    @Override
    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String getPayload() {
        return this.payload;
    }

    @Override
    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public Map<String, Object> getSoapFault() {
        return this.soapFault;
    }

    @Override
    public void setSoapFault(Map<String, Object> soapFault) {
        this.soapFault = soapFault;
    }

    @Override
    public Map<String, Object> getSoapHeaders() {
        return this.soapHeaders;
    }

    @Override
    public void setSoapHeaders(Map<String, Object> soapHeaders) {
        this.soapHeaders = soapHeaders;
    }
}
