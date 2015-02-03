package gov.hhs.onc.phiz.web.ws.logging;

import gov.hhs.onc.phiz.logging.logstash.MarkerObjectFieldName;

@MarkerObjectFieldName("wsRequestMessage")
public interface WsRequestMessageEvent extends WsMessageEvent {
}
