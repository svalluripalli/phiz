package gov.hhs.onc.phiz.web.ws.logging.impl;

import gov.hhs.onc.phiz.web.ws.logging.WsResponseMessageEvent;

public class WsResponseMessageEventImpl extends AbstractWsMessageEvent implements WsResponseMessageEvent {
    @Override
    public String toString() {
        return String.format("Web service (endpointAddr=%s) %s response (direction=%s, id=%d, soapHeaders=%s, soapFault=%s):\n%s", this.endpointAddr,
            this.endpointType.name().toLowerCase(), this.direction.name().toLowerCase(), this.eventId, this.soapHeaders, this.soapFault, this.payload);
    }
}
