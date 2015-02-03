package gov.hhs.onc.phiz.web.ws.feature.impl;

import com.github.sebhoss.warnings.CompilerWarnings;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import gov.hhs.onc.phiz.logging.logstash.impl.PhizLogstashMarkers;
import gov.hhs.onc.phiz.web.logging.HttpEvent;
import gov.hhs.onc.phiz.web.logging.HttpRequestEvent;
import gov.hhs.onc.phiz.web.logging.HttpResponseEvent;
import gov.hhs.onc.phiz.web.logging.impl.HttpRequestEventImpl;
import gov.hhs.onc.phiz.web.logging.impl.HttpResponseEventImpl;
import gov.hhs.onc.phiz.web.tomcat.impl.PhizTomcatEmbeddedServletContainerFactory.PhizRequest;
import gov.hhs.onc.phiz.web.tomcat.impl.PhizTomcatEmbeddedServletContainerFactory.PhizRequestFacade;
import gov.hhs.onc.phiz.web.tomcat.impl.PhizTomcatEmbeddedServletContainerFactory.PhizResponse;
import gov.hhs.onc.phiz.web.tomcat.impl.PhizTomcatEmbeddedServletContainerFactory.PhizResponseFacade;
import gov.hhs.onc.phiz.web.ws.PhizWsEndpointType;
import gov.hhs.onc.phiz.web.ws.PhizWsMessageContextProperties;
import gov.hhs.onc.phiz.web.ws.PhizWsMessageDirection;
import gov.hhs.onc.phiz.web.ws.interceptor.impl.AbstractPhizSoapInterceptor;
import gov.hhs.onc.phiz.web.ws.logging.WsMessageEvent;
import gov.hhs.onc.phiz.web.ws.logging.WsRequestMessageEvent;
import gov.hhs.onc.phiz.web.ws.logging.WsResponseMessageEvent;
import gov.hhs.onc.phiz.web.ws.logging.impl.WsRequestMessageEventImpl;
import gov.hhs.onc.phiz.web.ws.logging.impl.WsResponseMessageEventImpl;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsUtils;
import gov.hhs.onc.phiz.xml.utils.PhizXmlUtils;
import gov.hhs.onc.phiz.xml.utils.PhizXmlUtils.HideContentDomStreamFilter;
import gov.hhs.onc.phiz.xml.utils.PhizXmlUtils.IgnoreWhitespaceStreamFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.collections4.EnumerationUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.Headers;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PhizLoggingFeature extends AbstractFeature {
    private abstract class AbstractPhizLoggingInterceptor<T extends HttpEvent, U extends WsMessageEvent> extends AbstractPhizSoapInterceptor {
        protected Logger logger;
        protected Supplier<T> httpEventSupplier;
        protected Supplier<U> wsMsgEventSupplier;

        protected AbstractPhizLoggingInterceptor(String phase, Logger logger, Supplier<T> httpEventSupplier, Supplier<U> wsMsgEventSupplier) {
            super(phase);

            this.logger = logger;
            this.httpEventSupplier = httpEventSupplier;
            this.wsMsgEventSupplier = wsMsgEventSupplier;
        }

        @Override
        public void handleMessage(SoapMessage msg) throws Fault {
            if (msg.containsKey(WS_MSG_EVENT_ID_PROP_NAME)) {
                return;
            }

            Exchange msgExchange = msg.getExchange();

            if (!msgExchange.containsKey(WS_MSG_EVENT_ID_PROP_NAME)) {
                msgExchange.put(WS_MSG_EVENT_ID_PROP_NAME, WS_MSG_EVENT_ID.incrementAndGet());
            }

            // noinspection ConstantConditions
            int wsMsgEventId = PhizWsUtils.getProperty(msg.getExchange(), WS_MSG_EVENT_ID_PROP_NAME, Integer.class);
            msg.put(WS_MSG_EVENT_ID_PROP_NAME, wsMsgEventId);

            try {
                this.handleMessageInternal(msg, this.createHttpEvent(msg), this.createWsMessageEvent(msg, wsMsgEventId));
            } catch (Fault e) {
                throw e;
            } catch (Exception e) {
                throw new Fault(e);
            }
        }

        protected abstract void handleMessageInternal(SoapMessage msg, T httpEvent, U wsMsgEvent) throws Exception;

        protected U createWsMessageEvent(SoapMessage msg, int wsMsgEventId) {
            U wsMsgEvent = this.wsMsgEventSupplier.get();
            wsMsgEvent.setEndpointAddress(msg.getExchange().getEndpoint().getEndpointInfo().getAddress());
            wsMsgEvent.setEventId(wsMsgEventId);
            wsMsgEvent.setLogger(this.logger);

            return wsMsgEvent;
        }

        protected T createHttpEvent(SoapMessage msg) {
            return this.httpEventSupplier.get();
        }
    }

    private abstract class AbstractPhizLoggingInInterceptor<T extends HttpEvent, U extends WsMessageEvent> extends AbstractPhizLoggingInterceptor<T, U> {
        @SuppressWarnings({ CompilerWarnings.UNCHECKED })
        protected AbstractPhizLoggingInInterceptor(Logger logger, Supplier<T> httpEventSupplier, Supplier<U> wsMsgEventSupplier) {
            super(Phase.RECEIVE, logger, httpEventSupplier, wsMsgEventSupplier);

            this.setBefore(PolicyConstants.POLICY_IN_INTERCEPTOR_ID);
        }

        @Override
        protected void handleMessageInternal(SoapMessage msg, T httpEvent, U wsMsgEvent) throws Exception {
            try (InputStream msgPayloadInStream = PhizWsUtils.getCachedInputStream(msg)) {
                Document msgPayloadDoc = PhizXmlUtils.read(msgPayloadInStream, IgnoreWhitespaceStreamFilter.INSTANCE);
                Element msgPayloadDocElem = msgPayloadDoc.getDocumentElement();

                PhizLoggingFeature.populateSoapHeaders(wsMsgEvent, msgPayloadDocElem);
                PhizLoggingFeature.populateSoapFault(wsMsgEvent, msgPayloadDocElem);

                PhizLoggingFeature.this.logMessage(msg, httpEvent, wsMsgEvent, msgPayloadDoc);
            }
        }

        @Override
        protected U createWsMessageEvent(SoapMessage msg, int wsMsgEventId) {
            U wsMsgEvent = super.createWsMessageEvent(msg, wsMsgEventId);
            wsMsgEvent.setDirection(PhizWsMessageDirection.INBOUND);

            return wsMsgEvent;
        }
    }

    private class PhizServerLoggingInInterceptor extends AbstractPhizLoggingInInterceptor<HttpRequestEvent, WsRequestMessageEvent> {
        public PhizServerLoggingInInterceptor(Logger logger) {
            super(logger, HttpRequestEventImpl::new, WsRequestMessageEventImpl::new);
        }

        @Override
        public void handleMessage(SoapMessage msg) throws Fault {
            if (!Objects.equals(msg.get(Message.HTTP_REQUEST_METHOD), HttpMethod.POST.name())) {
                return;
            }

            super.handleMessage(msg);
        }

        @Override
        protected WsRequestMessageEvent createWsMessageEvent(SoapMessage msg, int wsMsgEventId) {
            WsRequestMessageEvent wsMsgEvent = super.createWsMessageEvent(msg, wsMsgEventId);
            wsMsgEvent.setEndpointType(PhizWsEndpointType.SERVER);

            return wsMsgEvent;
        }

        @Override
        protected HttpRequestEvent createHttpEvent(SoapMessage msg) {
            // noinspection ConstantConditions
            PhizRequest httpServletReq = PhizWsUtils.getProperty(msg, AbstractHTTPDestination.HTTP_REQUEST, PhizRequestFacade.class).getRequest();

            HttpRequestEvent httpEvent = super.createHttpEvent(msg);
            // noinspection ConstantConditions
            httpEvent.setAuthType(httpServletReq.getAuthType());
            httpEvent.setContentLength(httpServletReq.getContentLengthLong());
            httpEvent.setContentType(httpServletReq.getContentType());
            httpEvent.setContextPath(httpServletReq.getContextPath());
            httpEvent.setLocalName(httpServletReq.getLocalName());
            httpEvent.setLocalPort(httpServletReq.getLocalPort());
            httpEvent.setMethod(HttpMethod.POST);
            httpEvent.setPathInfo(httpServletReq.getPathInfo());
            httpEvent.setProtocol(httpServletReq.getProtocol());
            httpEvent.setQueryString(httpServletReq.getQueryString());
            httpEvent.setRemoteAddr(httpServletReq.getRemoteAddr());
            httpEvent.setRemoteHost(httpServletReq.getRemoteHost());
            httpEvent.setRemotePort(httpServletReq.getRemotePort());
            httpEvent.setScheme(httpServletReq.getScheme());
            httpEvent.setServerName(httpServletReq.getServerName());
            httpEvent.setServerPort(httpServletReq.getServerPort());
            httpEvent.setServletPath(httpServletReq.getServletPath());
            httpEvent.setUrl(httpServletReq.getRequestURL().toString());
            httpEvent.setUserPrincipal(Objects.toString(httpServletReq.getUserPrincipal(), null));

            HttpHeaders httpHeaders = httpEvent.getHeaders();

            EnumerationUtils.toList(httpServletReq.getHeaderNames()).stream()
                .forEach(httpHeaderName -> httpHeaders.put(httpHeaderName, EnumerationUtils.toList(httpServletReq.getHeaders(httpHeaderName))));

            return httpEvent;
        }
    }

    private class PhizClientLoggingInInterceptor extends AbstractPhizLoggingInInterceptor<HttpResponseEvent, WsResponseMessageEvent> {
        public PhizClientLoggingInInterceptor(Logger logger) {
            super(logger, HttpResponseEventImpl::new, WsResponseMessageEventImpl::new);
        }

        @Override
        protected WsResponseMessageEvent createWsMessageEvent(SoapMessage msg, int wsMsgEventId) {
            WsResponseMessageEvent wsMsgEvent = super.createWsMessageEvent(msg, wsMsgEventId);
            wsMsgEvent.setEndpointType(PhizWsEndpointType.CLIENT);

            return wsMsgEvent;
        }

        @Override
        protected HttpResponseEvent createHttpEvent(SoapMessage msg) {
            HttpResponseEvent httpEvent = super.createHttpEvent(msg);
            httpEvent.setContentType(PhizWsUtils.getProperty(msg, Message.CONTENT_TYPE));
            httpEvent.getHeaders().putAll(Headers.getSetProtocolHeaders(msg));
            // noinspection ConstantConditions
            httpEvent.setStatus(HttpStatus.valueOf(PhizWsUtils.getProperty(msg, Message.RESPONSE_CODE, Integer.class)));

            return httpEvent;
        }
    }

    private class PhizLoggingOutCallback<T extends HttpEvent, U extends WsMessageEvent> implements CachedOutputStreamCallback {
        private SoapMessage msg;
        private T httpEvent;
        private U wsMsgEvent;
        private BiConsumer<SoapMessage, T> httpEventPopulator;

        public PhizLoggingOutCallback(SoapMessage msg, T httpEvent, U wsMsgEvent, BiConsumer<SoapMessage, T> httpEventPopulator) {
            this.msg = msg;
            this.httpEvent = httpEvent;
            this.wsMsgEvent = wsMsgEvent;
            this.httpEventPopulator = httpEventPopulator;
        }

        @Override
        public void onClose(CachedOutputStream msgPayloadOutStream) {
            this.httpEventPopulator.accept(this.msg, this.httpEvent);

            try (InputStream msgPayloadInStream = msgPayloadOutStream.getInputStream()) {
                Document msgPayloadDoc = PhizXmlUtils.read(msgPayloadInStream, IgnoreWhitespaceStreamFilter.INSTANCE);
                Element msgPayloadDocElem = msgPayloadDoc.getDocumentElement();

                PhizLoggingFeature.populateSoapHeaders(this.wsMsgEvent, msgPayloadDocElem);
                PhizLoggingFeature.populateSoapFault(this.wsMsgEvent, msgPayloadDocElem);

                PhizLoggingFeature.this.logMessage(this.msg, this.httpEvent, this.wsMsgEvent, msgPayloadDoc);
            } catch (IOException | XMLStreamException e) {
                throw new Fault(e);
            }
        }

        @Override
        public void onFlush(CachedOutputStream msgPayloadOutStream) {
        }
    }

    private abstract class AbstractPhizLoggingOutInterceptor<T extends HttpEvent, U extends WsMessageEvent> extends AbstractPhizLoggingInterceptor<T, U> {
        @SuppressWarnings({ CompilerWarnings.UNCHECKED })
        protected AbstractPhizLoggingOutInterceptor(Logger logger, Supplier<T> httpEventSupplier, Supplier<U> wsMsgEventSupplier) {
            super(Phase.PRE_STREAM, logger, httpEventSupplier, wsMsgEventSupplier);

            this.setBeforeClasses(StaxOutInterceptor.class);
        }

        protected abstract PhizLoggingOutCallback<T, U> createCallback(SoapMessage msg, T httpEvent, U wsMsgEvent);

        @Override
        protected U createWsMessageEvent(SoapMessage msg, int wsMsgEventId) {
            U wsMsgEvent = super.createWsMessageEvent(msg, wsMsgEventId);
            wsMsgEvent.setDirection(PhizWsMessageDirection.OUTBOUND);

            return wsMsgEvent;
        }
    }

    private class PhizServerLoggingOutInterceptor extends AbstractPhizLoggingOutInterceptor<HttpResponseEvent, WsResponseMessageEvent> {
        public PhizServerLoggingOutInterceptor(Logger logger) {
            super(logger, HttpResponseEventImpl::new, WsResponseMessageEventImpl::new);
        }

        @Override
        protected void handleMessageInternal(SoapMessage msg, HttpResponseEvent httpEvent, WsResponseMessageEvent wsMsgEvent) throws Exception {
            CacheAndWriteOutputStream msgPayloadOutStream = new CacheAndWriteOutputStream(msg.getContent(OutputStream.class));
            msgPayloadOutStream.registerCallback(this.createCallback(msg, httpEvent, wsMsgEvent));
            msg.setContent(OutputStream.class, msgPayloadOutStream);
        }

        @Override
        protected PhizLoggingOutCallback<HttpResponseEvent, WsResponseMessageEvent> createCallback(SoapMessage msg, HttpResponseEvent httpEvent,
            WsResponseMessageEvent wsMsgEvent) {
            // noinspection ConstantConditions
            PhizResponse httpServletResp = PhizWsUtils.getProperty(msg, AbstractHTTPDestination.HTTP_RESPONSE, PhizResponseFacade.class).getResponse();

            return new PhizLoggingOutCallback<>(msg, httpEvent, wsMsgEvent, (callbackMsg, callbackHttpEvent) -> {
                // noinspection ConstantConditions
                httpEvent.setContentLength(httpServletResp.getCoyoteResponse().getContentLengthLong());
                // noinspection ConstantConditions
                httpEvent.setContentType(httpServletResp.getContentType());
                // noinspection ConstantConditions
                httpEvent.setStatus(HttpStatus.valueOf(httpServletResp.getStatus()));

                HttpHeaders httpHeaders = httpEvent.getHeaders();

                httpServletResp.getHeaderNames().stream()
                    .forEach(httpHeaderName -> httpHeaders.put(httpHeaderName, new ArrayList<>(httpServletResp.getHeaders(httpHeaderName))));
            });
        }

        @Override
        protected WsResponseMessageEvent createWsMessageEvent(SoapMessage msg, int wsMsgEventId) {
            WsResponseMessageEvent wsMsgEvent = super.createWsMessageEvent(msg, wsMsgEventId);
            wsMsgEvent.setEndpointType(PhizWsEndpointType.SERVER);

            return wsMsgEvent;
        }
    }

    private class PhizClientLoggingOutInterceptor extends AbstractPhizLoggingOutInterceptor<HttpRequestEvent, WsRequestMessageEvent> {
        public PhizClientLoggingOutInterceptor(Logger logger) {
            super(logger, HttpRequestEventImpl::new, WsRequestMessageEventImpl::new);
        }

        @Override
        public void handleMessage(SoapMessage msg) throws Fault {
            if (!Objects.equals(msg.get(Message.HTTP_REQUEST_METHOD), HttpMethod.POST.name())) {
                return;
            }

            super.handleMessage(msg);
        }

        @Override
        protected void handleMessageInternal(SoapMessage msg, HttpRequestEvent httpEvent, WsRequestMessageEvent wsMsgEvent) throws Exception {
            CacheAndWriteOutputStream msgPayloadOutStream = new CacheAndWriteOutputStream(msg.getContent(OutputStream.class));
            msgPayloadOutStream.registerCallback(this.createCallback(msg, httpEvent, wsMsgEvent));
            msg.setContent(OutputStream.class, msgPayloadOutStream);
        }

        @Override
        protected PhizLoggingOutCallback<HttpRequestEvent, WsRequestMessageEvent> createCallback(SoapMessage msg, HttpRequestEvent httpEvent,
            WsRequestMessageEvent wsMsgEvent) {
            return new PhizLoggingOutCallback<>(msg, httpEvent, wsMsgEvent, (callbackMsg, callbackHttpEvent) -> {
                httpEvent.setContentType(PhizWsUtils.getProperty(msg, Message.CONTENT_TYPE));
                httpEvent.getHeaders().putAll(Headers.getSetProtocolHeaders(msg));
                httpEvent.setMethod(HttpMethod.valueOf(PhizWsUtils.getProperty(msg, Message.HTTP_REQUEST_METHOD)));
                httpEvent.setPathInfo(PhizWsUtils.getProperty(msg, Message.PATH_INFO));
                httpEvent.setQueryString(PhizWsUtils.getProperty(msg, Message.QUERY_STRING));
                httpEvent.setUrl(PhizWsUtils.getProperty(msg, Message.REQUEST_URL));
            });
        }

        @Override
        protected WsRequestMessageEvent createWsMessageEvent(SoapMessage msg, int wsMsgEventId) {
            WsRequestMessageEvent wsMsgEvent = super.createWsMessageEvent(msg, wsMsgEventId);
            wsMsgEvent.setEndpointType(PhizWsEndpointType.CLIENT);

            return wsMsgEvent;
        }
    }

    public final static String WS_MSG_EVENT_ID_PROP_NAME = "wsMsgEventId";

    private final static AtomicInteger WS_MSG_EVENT_ID = new AtomicInteger();

    private final static Logger SERVER_LOGGER = LoggerFactory.getLogger(Server.class);
    private final static Logger CLIENT_LOGGER = LoggerFactory.getLogger(Client.class);

    private int indentSize;

    @Override
    public void initialize(Server server, Bus bus) {
        Service service = server.getEndpoint().getService();

        PhizServerLoggingInInterceptor loggingInInterceptor = new PhizServerLoggingInInterceptor(SERVER_LOGGER);
        service.getInInterceptors().add(loggingInInterceptor);
        service.getInFaultInterceptors().add(loggingInInterceptor);

        PhizServerLoggingOutInterceptor loggingOutInterceptor = new PhizServerLoggingOutInterceptor(SERVER_LOGGER);
        service.getOutInterceptors().add(loggingOutInterceptor);
        service.getOutFaultInterceptors().add(loggingOutInterceptor);
    }

    @Override
    public void initialize(Client client, Bus bus) {
        PhizClientLoggingInInterceptor loggingInInterceptor = new PhizClientLoggingInInterceptor(CLIENT_LOGGER);
        client.getInInterceptors().add(loggingInInterceptor);
        client.getInFaultInterceptors().add(loggingInInterceptor);

        PhizClientLoggingOutInterceptor loggingOutInterceptor = new PhizClientLoggingOutInterceptor(CLIENT_LOGGER);
        client.getOutInterceptors().add(loggingOutInterceptor);
        client.getOutFaultInterceptors().add(loggingOutInterceptor);
    }

    private static void populateSoapFault(WsMessageEvent wsMsgEvent, Element msgPayloadDocElem) {
        Element msgSoapFaultElem =
            DOMUtils
                .getFirstChildWithName(DOMUtils.getFirstChildWithName(msgPayloadDocElem, SOAP12Constants.QNAME_SOAP_BODY), SOAP12Constants.QNAME_SOAP_FAULT);

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

    private static void populateSoapHeaders(WsMessageEvent wsMsgEvent, Element msgPayloadDocElem) {
        Element[] msgSoapHeaderElems =
            PhizXmlUtils.findElements(msgPayloadDocElem, SOAP12Constants.QNAME_SOAP_HEADER).stream()
                .flatMap((msgSoapHeaderContainerElem) -> DomUtils.getChildElements(msgSoapHeaderContainerElem).stream()).toArray(Element[]::new);

        if (msgSoapHeaderElems.length > 0) {
            wsMsgEvent.setSoapHeaders(PhizXmlUtils.mapTreeContent(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER), msgSoapHeaderElems));
        }
    }

    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    private void logMessage(SoapMessage msg, HttpEvent httpEvent, WsMessageEvent wsMsgEvent, Document msgPayloadDoc) throws XMLStreamException {
        msg.getContextualProperty(null);

        wsMsgEvent.setPayload((PhizWsUtils.hasContextualProperty(msg, PhizWsMessageContextProperties.LOG_MSG_PAYLOAD_HIDE_CONTENT_ELEM_QNAMES) ? PhizXmlUtils
            .toString(msgPayloadDoc, this.indentSize,
                new HideContentDomStreamFilter(
                    ((Set<QName>) msg.getContextualProperty(PhizWsMessageContextProperties.LOG_MSG_PAYLOAD_HIDE_CONTENT_ELEM_QNAMES)))) : PhizXmlUtils
            .toString(msgPayloadDoc, this.indentSize)));

        wsMsgEvent.getLogger().info(PhizLogstashMarkers.append(httpEvent, wsMsgEvent), wsMsgEvent.toString());
    }

    public int getIndentSize() {
        return this.indentSize;
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }
}
