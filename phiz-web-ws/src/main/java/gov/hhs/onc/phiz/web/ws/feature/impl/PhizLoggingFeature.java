package gov.hhs.onc.phiz.web.ws.feature.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.sebhoss.warnings.CompilerWarnings;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import gov.hhs.onc.phiz.logging.logstash.MarkerObjectFieldName;
import gov.hhs.onc.phiz.logging.logstash.impl.PhizLogstashMarkers;
import gov.hhs.onc.phiz.web.PhizHttpRequestMethods;
import gov.hhs.onc.phiz.web.logging.HttpEvent;
import gov.hhs.onc.phiz.web.logging.HttpRequestEvent;
import gov.hhs.onc.phiz.web.logging.HttpResponseEvent;
import gov.hhs.onc.phiz.web.logging.impl.HttpRequestEventImpl;
import gov.hhs.onc.phiz.web.logging.impl.HttpResponseEventImpl;
import gov.hhs.onc.phiz.web.ws.PhizWsMessageContextProperties;
import gov.hhs.onc.phiz.web.ws.PhizWsMessageDirection;
import gov.hhs.onc.phiz.web.ws.interceptor.impl.AbstractPhizSoapInterceptor;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsUtils;
import gov.hhs.onc.phiz.xml.utils.PhizXmlUtils;
import gov.hhs.onc.phiz.xml.utils.PhizXmlUtils.HideContentDomStreamFilter;
import gov.hhs.onc.phiz.xml.utils.PhizXmlUtils.IgnoreWhitespaceStreamFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PhizLoggingFeature extends AbstractFeature {
    private static abstract class AbstractWsMessageEvent {
        protected int eventId;
        protected String endpointAddr;
        protected Map<String, Object> soapFault;
        protected Map<String, Object> soapHeaders;
        protected String payload;

        protected AbstractWsMessageEvent(String endpointAddr, int eventId) {
            this.endpointAddr = endpointAddr;
            this.eventId = eventId;
        }

        @Override
        public String toString() {
            return String.format("%s message (eventId=%d, endpointAddr=%s, soapHeaders=%s, soapFault=%s):\n%s",
                StringUtils.capitalize(this.getDirection().name().toLowerCase()), this.eventId, this.endpointAddr, this.soapHeaders, this.soapFault,
                this.payload);
        }

        @JsonProperty
        public String getEndpointAddress() {
            return this.endpointAddr;
        }

        @JsonProperty
        public int getEventId() {
            return this.eventId;
        }

        public abstract PhizWsMessageDirection getDirection();

        @JsonProperty
        public String getPayload() {
            return this.payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        @JsonProperty
        public Map<String, Object> getSoapFault() {
            return this.soapFault;
        }

        public void setSoapFault(Map<String, Object> soapFault) {
            this.soapFault = soapFault;
        }

        @JsonProperty
        public Map<String, Object> getSoapHeaders() {
            return this.soapHeaders;
        }

        public void setSoapHeaders(Map<String, Object> soapHeaders) {
            this.soapHeaders = soapHeaders;
        }
    }

    @MarkerObjectFieldName("wsRequest")
    private static class WsRequestMessageEvent extends AbstractWsMessageEvent {
        public WsRequestMessageEvent(String endpointAddr, int eventId) {
            super(endpointAddr, eventId);
        }

        @Override
        public PhizWsMessageDirection getDirection() {
            return PhizWsMessageDirection.INBOUND;
        }
    }

    @MarkerObjectFieldName("wsResponse")
    private static class WsResponseMessageEvent extends AbstractWsMessageEvent {
        public WsResponseMessageEvent(String endpointAddr, int eventId) {
            super(endpointAddr, eventId);
        }

        @Override
        public PhizWsMessageDirection getDirection() {
            return PhizWsMessageDirection.OUTBOUND;
        }
    }

    private abstract class AbstractPhizLoggingInterceptor<T, U extends HttpEvent<T>, V extends AbstractWsMessageEvent> extends AbstractPhizSoapInterceptor {
        protected Function<Message, T> httpEventDescSupplier;
        protected Function<T, U> httpEventSupplier;
        protected BiFunction<String, Integer, V> wsMsgEventSupplier;

        protected AbstractPhizLoggingInterceptor(String phase, Function<Message, T> httpEventDescSupplier, Function<T, U> httpEventSupplier,
            BiFunction<String, Integer, V> wsMsgEventSupplier) {
            super(phase);

            this.httpEventDescSupplier = httpEventDescSupplier;
            this.httpEventSupplier = httpEventSupplier;
            this.wsMsgEventSupplier = wsMsgEventSupplier;
        }

        @Override
        public void handleMessage(SoapMessage msg) throws Fault {
            Exchange msgExchange = msg.getExchange();

            if (!msgExchange.containsKey(WS_MSG_EVENT_ID_PROP_NAME)) {
                msgExchange.put(WS_MSG_EVENT_ID_PROP_NAME, WS_MSG_EVENT_ID.incrementAndGet());
            }

            try {
                this.handleMessageInternal(
                    msg,
                    this.httpEventSupplier.apply(this.httpEventDescSupplier.apply(msg)),
                    this.wsMsgEventSupplier.apply(msg.getExchange().getEndpoint().getEndpointInfo().getAddress(),
                        PhizWsUtils.getProperty(msgExchange, WS_MSG_EVENT_ID_PROP_NAME, Integer.class)));
            } catch (Fault e) {
                throw e;
            } catch (Exception e) {
                throw new Fault(e);
            }
        }

        protected abstract void handleMessageInternal(SoapMessage msg, U httpEvent, V wsMsgEvent) throws Exception;

        protected void populateSoapFault(V wsMsgEvent, Element msgPayloadDocElem) {
            Element msgSoapFaultElem =
                DOMUtils.getFirstChildWithName(DOMUtils.getFirstChildWithName(msgPayloadDocElem, SOAP12Constants.QNAME_SOAP_BODY),
                    SOAP12Constants.QNAME_SOAP_FAULT);

            if (msgSoapFaultElem != null) {
                Map<String, Object> msgSoapFaultContentMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

                Optional.ofNullable(DOMUtils.getFirstChildWithName(msgSoapFaultElem, SOAP12Constants.QNAME_FAULT_CODE)).ifPresent(
                    (msgSoapFaultCodeElem) -> msgSoapFaultContentMap.put(SOAP12Constants.QNAME_FAULT_CODE.getLocalPart(),
                        DOMUtils.getContent(DOMUtils.getFirstChildWithName(msgSoapFaultCodeElem, SOAP12Constants.QNAME_FAULT_VALUE))));

                List<Element> msgSoapFaultSubcodeElems = PhizXmlUtils.findElements(msgSoapFaultElem, SOAP12Constants.QNAME_FAULT_SUBCODE);

                if (!msgSoapFaultSubcodeElems.isEmpty()) {
                    msgSoapFaultContentMap.put(
                        SOAP12Constants.QNAME_FAULT_SUBCODE.getLocalPart(),
                        msgSoapFaultSubcodeElems
                            .stream()
                            .map(
                                (msgSoapFaultSubcodeElem) -> DOMUtils.getContent(DOMUtils.getFirstChildWithName(msgSoapFaultSubcodeElem,
                                    SOAP12Constants.QNAME_FAULT_VALUE))).collect(Collectors.toList()));
                }

                Optional.ofNullable(DOMUtils.getFirstChildWithName(msgSoapFaultElem, SOAP12Constants.QNAME_FAULT_REASON)).ifPresent(
                    (msgSoapFaultReasonElem) -> msgSoapFaultContentMap.put(SOAP12Constants.QNAME_FAULT_REASON.getLocalPart(),
                        DOMUtils.getContent(DOMUtils.getFirstChildWithName(msgSoapFaultReasonElem, SOAP12Constants.QNAME_FAULT_REASON_TEXT))));

                Optional.ofNullable(DOMUtils.getFirstChildWithName(msgSoapFaultElem, SOAP12Constants.QNAME_FAULT_DETAIL)).ifPresent(
                    (msgSoapFaultDetailElem) -> {
                        List<Element> msgSoapFaultDetailChildElems = DomUtils.getChildElements(msgSoapFaultDetailElem);

                        if (!msgSoapFaultDetailChildElems.isEmpty()) {
                            msgSoapFaultContentMap.put(
                                SOAP12Constants.QNAME_FAULT_DETAIL.getLocalPart(),
                                PhizXmlUtils.mapTreeContent(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                                    msgSoapFaultDetailChildElems.toArray(new Element[msgSoapFaultDetailChildElems.size()])));
                        }
                    });

                wsMsgEvent.setSoapFault(msgSoapFaultContentMap);
            }
        }

        protected void populateSoapHeaders(V wsMsgEvent, Element msgPayloadDocElem) {
            Element[] msgSoapHeaderElems =
                PhizXmlUtils.findElements(msgPayloadDocElem, SOAP12Constants.QNAME_SOAP_HEADER).stream()
                    .flatMap((msgSoapHeaderContainerElem) -> DomUtils.getChildElements(msgSoapHeaderContainerElem).stream()).toArray(Element[]::new);

            if (msgSoapHeaderElems.length > 0) {
                wsMsgEvent.setSoapHeaders(PhizXmlUtils.mapTreeContent(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER), msgSoapHeaderElems));
            }
        }
    }

    private class PhizLoggingInInterceptor extends AbstractPhizLoggingInterceptor<HttpServletRequest, HttpRequestEvent, WsRequestMessageEvent> {
        public PhizLoggingInInterceptor() {
            super(Phase.RECEIVE, PhizWsUtils::getHttpServletRequest, HttpRequestEventImpl::new, WsRequestMessageEvent::new);
        }

        @Override
        public void handleMessage(SoapMessage msg) throws Fault {
            if (!Objects.equals(msg.get(Message.HTTP_REQUEST_METHOD), PhizHttpRequestMethods.POST) || msg.containsKey(WS_MSG_EVENT_ID_PROP_NAME)) {
                return;
            }

            super.handleMessage(msg);
        }

        @Override
        protected void handleMessageInternal(SoapMessage msg, HttpRequestEvent httpEvent, WsRequestMessageEvent wsMsgEvent) throws Exception {
            msg.put(WS_MSG_EVENT_ID_PROP_NAME, wsMsgEvent.getEventId());

            try (InputStream msgPayloadInStream = PhizWsUtils.getCachedInputStream(msg)) {
                Document msgPayloadDoc = PhizXmlUtils.read(msgPayloadInStream, IgnoreWhitespaceStreamFilter.INSTANCE);
                Element msgPayloadDocElem = msgPayloadDoc.getDocumentElement();

                this.populateSoapHeaders(wsMsgEvent, msgPayloadDocElem);
                this.populateSoapFault(wsMsgEvent, msgPayloadDocElem);

                PhizLoggingFeature.this.logMessage(msg, httpEvent, wsMsgEvent, msgPayloadDoc);
            }
        }
    }

    private class PhizLoggingOutCallback implements CachedOutputStreamCallback {
        private SoapMessage msg;
        private HttpResponseEvent httpEvent;
        private WsResponseMessageEvent wsMsgEvent;
        private BiConsumer<WsResponseMessageEvent, Element> populateSoapHeadersConsumer;
        private BiConsumer<WsResponseMessageEvent, Element> populateSoapFaultsConsumer;

        public PhizLoggingOutCallback(SoapMessage msg, HttpResponseEvent httpEvent, WsResponseMessageEvent wsMsgEvent,
            BiConsumer<WsResponseMessageEvent, Element> populateSoapHeadersConsumer, BiConsumer<WsResponseMessageEvent, Element> populateSoapFaultsConsumer) {
            this.msg = msg;
            this.httpEvent = httpEvent;
            this.wsMsgEvent = wsMsgEvent;
            this.populateSoapHeadersConsumer = populateSoapHeadersConsumer;
            this.populateSoapFaultsConsumer = populateSoapFaultsConsumer;
        }

        @Override
        public void onClose(CachedOutputStream msgPayloadOutStream) {
            try (InputStream msgPayloadInStream = msgPayloadOutStream.getInputStream()) {
                Document msgPayloadDoc = PhizXmlUtils.read(msgPayloadInStream, IgnoreWhitespaceStreamFilter.INSTANCE);
                Element msgPayloadDocElem = msgPayloadDoc.getDocumentElement();

                this.populateSoapHeadersConsumer.accept(this.wsMsgEvent, msgPayloadDocElem);
                this.populateSoapFaultsConsumer.accept(this.wsMsgEvent, msgPayloadDocElem);

                PhizLoggingFeature.this.logMessage(this.msg, this.httpEvent, this.wsMsgEvent, msgPayloadDoc);
            } catch (IOException | XMLStreamException e) {
                throw new Fault(e);
            }
        }

        @Override
        public void onFlush(CachedOutputStream msgPayloadOutStream) {
        }
    }

    private class PhizLoggingOutInterceptor extends AbstractPhizLoggingInterceptor<HttpServletResponse, HttpResponseEvent, WsResponseMessageEvent> {
        @SuppressWarnings({ CompilerWarnings.UNCHECKED })
        public PhizLoggingOutInterceptor() {
            super(Phase.PRE_STREAM, PhizWsUtils::getHttpServletResponse, HttpResponseEventImpl::new, WsResponseMessageEvent::new);

            this.setBeforeClasses(StaxOutInterceptor.class);
        }

        @Override
        protected void handleMessageInternal(SoapMessage msg, HttpResponseEvent httpEvent, WsResponseMessageEvent wsMsgEvent) throws Exception {
            CacheAndWriteOutputStream msgPayloadOutStream = new CacheAndWriteOutputStream(msg.getContent(OutputStream.class));
            msgPayloadOutStream.registerCallback(new PhizLoggingOutCallback(msg, httpEvent, wsMsgEvent, this::populateSoapHeaders, this::populateSoapFault));
            msg.setContent(OutputStream.class, msgPayloadOutStream);
        }
    }

    private final static String WS_MSG_EVENT_ID_PROP_NAME = "wsMsgEventId";
    private final static AtomicInteger WS_MSG_EVENT_ID = new AtomicInteger();

    private final static Logger LOGGER = LoggerFactory.getLogger(PhizLoggingFeature.class);

    private int indentSize;

    @Override
    protected void initializeProvider(InterceptorProvider interceptorProv, Bus bus) {
        PhizLoggingInInterceptor loggingInInterceptor = new PhizLoggingInInterceptor();
        interceptorProv.getInInterceptors().add(loggingInInterceptor);
        interceptorProv.getInInterceptors().add(loggingInInterceptor);

        PhizLoggingOutInterceptor loggingOutInterceptor = new PhizLoggingOutInterceptor();
        interceptorProv.getOutInterceptors().add(loggingOutInterceptor);
        interceptorProv.getOutFaultInterceptors().add(loggingOutInterceptor);
    }

    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    private void logMessage(SoapMessage msg, HttpEvent<?> httpEvent, AbstractWsMessageEvent wsMsgEvent, Document msgPayloadDoc) throws XMLStreamException {
        wsMsgEvent.setPayload((msg.getContextualPropertyKeys().contains(PhizWsMessageContextProperties.LOG_MSG_PAYLOAD_HIDE_CONTENT_ELEM_QNAMES) ? PhizXmlUtils
            .toString(msgPayloadDoc, this.indentSize,
                new HideContentDomStreamFilter(
                    ((Set<QName>) msg.getContextualProperty(PhizWsMessageContextProperties.LOG_MSG_PAYLOAD_HIDE_CONTENT_ELEM_QNAMES)))) : PhizXmlUtils
            .toString(msgPayloadDoc, this.indentSize)));

        LOGGER.info(PhizLogstashMarkers.append(httpEvent, wsMsgEvent), wsMsgEvent.toString());
    }

    public int getIndentSize() {
        return this.indentSize;
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }
}
