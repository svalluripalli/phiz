package gov.hhs.onc.phiz.web.ws.logging;

import gov.hhs.onc.phiz.logging.logstash.MarkerObjectFieldName;

@MarkerObjectFieldName("wsResponseMessage")
public interface WsResponseMessageEvent extends WsMessageEvent {
}
