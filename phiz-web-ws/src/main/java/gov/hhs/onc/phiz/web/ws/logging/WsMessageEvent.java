package gov.hhs.onc.phiz.web.ws.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.hhs.onc.phiz.web.ws.PhizWsEndpointType;
import gov.hhs.onc.phiz.web.ws.PhizWsMessageDirection;
import java.util.Map;
import org.slf4j.Logger;

public interface WsMessageEvent {
    @JsonProperty
    public PhizWsMessageDirection getDirection();

    public void setDirection(PhizWsMessageDirection direction);

    @JsonProperty
    public String getEndpointAddress();

    public void setEndpointAddress(String endpointAddr);

    @JsonProperty
    public PhizWsEndpointType getEndpointType();

    public void setEndpointType(PhizWsEndpointType endpointType);

    @JsonProperty
    public int getEventId();

    public void setEventId(int eventId);

    public Logger getLogger();

    public void setLogger(Logger logger);

    @JsonProperty
    public String getPayload();

    public void setPayload(String payload);

    @JsonProperty
    public Map<String, Object> getSoapFault();

    public void setSoapFault(Map<String, Object> soapFault);

    @JsonProperty
    public Map<String, Object> getSoapHeaders();

    public void setSoapHeaders(Map<String, Object> soapHeaders);
}
