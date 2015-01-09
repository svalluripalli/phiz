package gov.hhs.onc.phiz.web.ws.data.queue.impl;

import com.github.sebhoss.warnings.CompilerWarnings;
import gov.hhs.onc.phiz.data.queue.impl.AbstractPhizMessageConverter;
import gov.hhs.onc.phiz.web.servlet.utils.PhizServletUtils;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsAddressingUtils;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsUtils;
import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapHeaderInterceptor;
import org.apache.cxf.binding.soap.interceptor.StartBodyInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerRegistry;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.addressing.impl.MAPAggregatorImpl;
import org.apache.cxf.ws.addressing.soap.MAPCodec;
import org.apache.cxf.wsdl.interceptors.DocLiteralInInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Component("msgConvSoap")
public class SoapMessageConverter extends AbstractPhizMessageConverter<SoapMessage> {
    private final static Logger LOGGER = LoggerFactory.getLogger(SoapMessageConverter.class);

    private final static String MSG_PROP_NAME_DELIM = ".";

    private final static String MSG_PROP_NAME_PREFIX = "phiz" + MSG_PROP_NAME_DELIM;
    private final static String HTTP_MSG_PROP_NAME_PREFIX = MSG_PROP_NAME_PREFIX + "http" + MSG_PROP_NAME_DELIM;
    private final static String HTTP_LOCAL_MSG_PROP_NAME_PREFIX = HTTP_MSG_PROP_NAME_PREFIX + "local" + MSG_PROP_NAME_DELIM;
    private final static String HTTP_REMOTE_MSG_PROP_NAME_PREFIX = HTTP_MSG_PROP_NAME_PREFIX + "remote" + MSG_PROP_NAME_DELIM;
    private final static String HTTP_SERVER_MSG_PROP_NAME_PREFIX = HTTP_MSG_PROP_NAME_PREFIX + "server" + MSG_PROP_NAME_DELIM;
    private final static String HTTP_HEADER_MSG_PROP_NAME_PREFIX = HTTP_MSG_PROP_NAME_PREFIX + "header" + MSG_PROP_NAME_DELIM;
    private final static String WS_MSG_PROP_NAME_PREFIX = MSG_PROP_NAME_PREFIX + "ws" + MSG_PROP_NAME_DELIM;

    private final static String NAME_MSG_PROP_NAME_SUFFIX = "name";
    private final static String PATH_MSG_PROP_NAME_SUFFIX = "path";
    private final static String PORT_MSG_PROP_NAME_SUFFIX = "port";

    private final static String HTTP_AUTH_TYPE_MSG_PROP_NAME = HTTP_MSG_PROP_NAME_PREFIX + "auth.type";
    private final static String HTTP_CONTEXT_PATH_MSG_PROP_NAME = HTTP_MSG_PROP_NAME_PREFIX + "context" + MSG_PROP_NAME_DELIM + PATH_MSG_PROP_NAME_SUFFIX;
    private final static String HTTP_LOCAL_NAME_MSG_PROP_NAME = HTTP_LOCAL_MSG_PROP_NAME_PREFIX + NAME_MSG_PROP_NAME_SUFFIX;
    private final static String HTTP_LOCAL_PORT_MSG_PROP_NAME = HTTP_LOCAL_MSG_PROP_NAME_PREFIX + PORT_MSG_PROP_NAME_SUFFIX;
    private final static String HTTP_METHOD_MSG_PROP_NAME = HTTP_MSG_PROP_NAME_PREFIX + "method";
    private final static String HTTP_PATH_INFO_MSG_PROP_NAME = HTTP_MSG_PROP_NAME_PREFIX + PATH_MSG_PROP_NAME_SUFFIX;
    private final static String HTTP_PROTOCOL_MSG_PROP_NAME = HTTP_MSG_PROP_NAME_PREFIX + "protocol";
    private final static String HTTP_QUERY_MSG_PROP_NAME = HTTP_MSG_PROP_NAME_PREFIX + "query";
    private final static String HTTP_REMOTE_ADDR_MSG_PROP_NAME = HTTP_REMOTE_MSG_PROP_NAME_PREFIX + "address";
    private final static String HTTP_REMOTE_PORT_MSG_PROP_NAME = HTTP_REMOTE_MSG_PROP_NAME_PREFIX + PORT_MSG_PROP_NAME_SUFFIX;
    private final static String HTTP_SCHEME_MSG_PROP_NAME = HTTP_MSG_PROP_NAME_PREFIX + "scheme";
    private final static String HTTP_SERVER_NAME_MSG_PROP_NAME = HTTP_SERVER_MSG_PROP_NAME_PREFIX + NAME_MSG_PROP_NAME_SUFFIX;
    private final static String HTTP_SERVER_PORT_MSG_PROP_NAME = HTTP_SERVER_MSG_PROP_NAME_PREFIX + PORT_MSG_PROP_NAME_SUFFIX;
    private final static String HTTP_SERVLET_PATH_MSG_PROP_NAME = HTTP_MSG_PROP_NAME_PREFIX + "servlet" + MSG_PROP_NAME_DELIM + PATH_MSG_PROP_NAME_SUFFIX;
    private final static String HTTP_STATUS_MSG_PROP_NAME = HTTP_MSG_PROP_NAME_PREFIX + "status";
    private final static String HTTP_USER_PRINCIPAL_MSG_PROP_NAME = HTTP_MSG_PROP_NAME_PREFIX + "user" + MSG_PROP_NAME_DELIM + "principal";

    private final static String WS_MSG_INBOUND_MSG_PROP_NAME = WS_MSG_PROP_NAME_PREFIX + "msg" + MSG_PROP_NAME_DELIM + "inbound";
    private final static String WS_MSG_REQ_ROLE_MSG_PROP_NAME = WS_MSG_PROP_NAME_PREFIX + "msg" + MSG_PROP_NAME_DELIM + "requestor.role";

    private final static String WS_SERVICE_NAME_MSG_PROP_NAME = WS_MSG_PROP_NAME_PREFIX + "service" + MSG_PROP_NAME_DELIM + NAME_MSG_PROP_NAME_SUFFIX;
    private final static String WS_OP_NAME_MSG_PROP_NAME = WS_MSG_PROP_NAME_PREFIX + "op" + MSG_PROP_NAME_DELIM + NAME_MSG_PROP_NAME_SUFFIX;

    @Resource(name = "charsetUtf8")
    private Charset charsetEnc;

    @Resource(name = "soapVersion12")
    private SoapVersion soapVersion;

    @Resource(name = "dataBindingJaxb")
    private JAXBDataBinding dataBinding;

    @Resource(name = "busPhiz")
    private Bus bus;

    public SoapMessageConverter() {
        super(SoapMessage.class);
    }

    @Override
    protected SoapMessage fromMessageInternal(Message msg) throws Exception {
        MessageProperties msgProps = msg.getMessageProperties();
        Map<String, Object> msgPropHeaders = msgProps.getHeaders();

        SoapMessage msgPayload = new SoapMessage(this.soapVersion);
        msgPayload.setId(msgProps.getMessageId());
        msgPayload.setContent(InputStream.class, new ByteArrayInputStream(msg.getBody()));
        msgPayload.put(org.apache.cxf.message.Message.ENCODING, msgProps.getContentEncoding());
        msgPayload.put(org.apache.cxf.message.Message.CONTENT_TYPE, msgProps.getContentType());

        boolean msgPayloadInbound = ((Boolean) msgPropHeaders.get(WS_MSG_INBOUND_MSG_PROP_NAME)), msgPayloadRequestor =
            ((Boolean) msgPropHeaders.get(WS_MSG_REQ_ROLE_MSG_PROP_NAME)), msgPayloadResp = (!msgPayloadInbound ^ msgPayloadRequestor);
        msgPayload.put(org.apache.cxf.message.Message.INBOUND_MESSAGE, msgPayloadInbound);
        // msgPayload.put(org.apache.cxf.message.Message.REQUESTOR_ROLE, msgPayloadRequestor);

        Exchange msgPayloadExchange = new ExchangeImpl() {
            private final static long serialVersionUID = 0L;

            {
                this.setInMessage(msgPayload);

                if (!msgPayloadInbound) {
                    this.setOutMessage(msgPayload);
                }

                Endpoint endpoint;
                Service service;
                QName serviceName;
                Binding binding;
                BindingInfo bindingInfo;
                BindingOperationInfo bindingOpInfo;

                for (Server server : SoapMessageConverter.this.bus.getExtension(ServerRegistry.class).getServers()) {
                    if ((serviceName = (service = (endpoint = server.getEndpoint()).getService()).getName()).toString().equals(
                        msgPropHeaders.get(WS_SERVICE_NAME_MSG_PROP_NAME))) {
                        this.put(Endpoint.class, endpoint);
                        this.put(Service.class, service);
                        this.put(Binding.class, (binding = endpoint.getBinding()));
                        this.put(BindingInfo.class, (bindingInfo = binding.getBindingInfo()));
                        this.put(BindingOperationInfo.class,
                            (bindingOpInfo = bindingInfo.getOperation(QName.valueOf(((String) msgPropHeaders.get(WS_OP_NAME_MSG_PROP_NAME))))));

                        msgPayload.put(org.apache.cxf.message.Message.WSDL_SERVICE, serviceName);
                        msgPayload.put(org.apache.cxf.message.Message.WSDL_OPERATION, bindingOpInfo.getName());

                        break;
                    }
                }
            }

            @Override
            public Binding getBinding() {
                return this.get(Binding.class);
            }

            @Override
            public BindingOperationInfo getBindingOperationInfo() {
                return this.get(BindingOperationInfo.class);
            }

            @Override
            public Bus getBus() {
                return SoapMessageConverter.this.bus;
            }

            @Override
            public Endpoint getEndpoint() {
                return this.get(Endpoint.class);
            }

            @Override
            public Service getService() {
                return this.get(Service.class);
            }
        };

        msgPayload.setExchange(msgPayloadExchange);

        HttpHeaders msgPayloadHttpHeaders = new HttpHeaders();

        msgPropHeaders
            .entrySet()
            .stream()
            .filter((msgPropHeaderEntry) -> msgPropHeaderEntry.getKey().startsWith(HTTP_HEADER_MSG_PROP_NAME_PREFIX))
            .map(
                (msgPropHeaderEntry) -> new ImmutablePair<>(StringUtils.removeStart(msgPropHeaderEntry.getKey(), HTTP_HEADER_MSG_PROP_NAME_PREFIX), Objects
                    .toString(msgPropHeaderEntry.getValue(), null)))
            .forEach((msgPayloadHttpHeader) -> msgPayloadHttpHeaders.add(msgPayloadHttpHeader.getKey(), msgPayloadHttpHeader.getValue()));
        Set<String> msgPayloadHttpHeaderNames = msgPayloadHttpHeaders.keySet();

        InvocationHandler msgPayloadServletHandlerInvocationHandler = (obj, method, args) -> {
            String methodName = method.getName();
            Class<?> methodReturnType = method.getReturnType();

            switch (methodName) {
                case "equals":
                    return (args[0] == this);

                case "hashCode":
                    return System.identityHashCode(obj);

                case "getHeader":
                    // noinspection SuspiciousMethodCalls
            return msgPayloadHttpHeaders.get(args[0]);

                case "getHeaderNames":
                    return (ClassUtils.isAssignable(methodReturnType, Enumeration.class)
                        ? new IteratorEnumeration<>(msgPayloadHttpHeaderNames.iterator()) : msgPayloadHttpHeaderNames);

                case "getHeaders":
                    // noinspection SuspiciousMethodCalls
            String[] headerValues =
                (msgPayloadHttpHeaders.containsKey(args[0])
                    ? msgPayloadHttpHeaders.get(args[0]).stream().toArray(String[]::new) : ArrayUtils.EMPTY_STRING_ARRAY);

                    return (ClassUtils.isAssignable(methodReturnType, Enumeration.class)
                        ? new IteratorEnumeration<>(IteratorUtils.arrayIterator(headerValues)) : Arrays.asList(headerValues));

                default:
                    PropertyDescriptor invocationPropDesc =
                        Stream.of(new BeanWrapperImpl(obj).getPropertyDescriptors())
                            .filter((propDesc) -> propDesc.getReadMethod().getName().equals(methodName)).findFirst().orElse(null);

                    // noinspection ConstantConditions
                    return ((invocationPropDesc != null) ? msgPropHeaders.get((HTTP_MSG_PROP_NAME_PREFIX + StringUtils.join(
                        StringUtils.splitByCharacterTypeCamelCase(invocationPropDesc.getName()), MSG_PROP_NAME_DELIM).toLowerCase())) : null);
            }
        };

        msgPayload.put(org.apache.cxf.message.Message.PROTOCOL_HEADERS, msgPayloadHttpHeaders);

        HttpServletRequest msgPayloadServletReq =
            ((HttpServletRequest) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { HttpServletRequest.class },
                msgPayloadServletHandlerInvocationHandler));

        msgPayload.put(AbstractHTTPDestination.HTTP_REQUEST, msgPayloadServletReq);

        HttpServletResponse msgPayloadServletResp = null;

        if (!msgPayloadInbound) {
            msgPayload.put(
                AbstractHTTPDestination.HTTP_RESPONSE,
                (msgPayloadServletResp =
                    ((HttpServletResponse) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { HttpServletResponse.class },
                        msgPayloadServletHandlerInvocationHandler))));
        }

        // TEMP: dev
        LOGGER.warn(String.format("msgPayloadContent=%s", IOUtils.toString(msg.getBody(), this.charsetEnc.name())));

        MAPCodec mapCodec = new MAPCodec();
        InterceptorChain msgPayloadInterceptorChain = new PhaseInterceptorChain(this.bus.getExtension(PhaseManager.class).getInPhases());
        msgPayloadInterceptorChain
            .add(Arrays.asList(new StaxInInterceptor(), new SoapActionInInterceptor(), new DocLiteralInInterceptor(), new StartBodyInterceptor(),
                new SoapHeaderInterceptor(), new ReadHeadersInterceptor(this.bus, this.soapVersion), new MAPAggregatorImpl(), mapCodec));
        msgPayload.setInterceptorChain(msgPayloadInterceptorChain);

        msgPayloadInterceptorChain.doIntercept(msgPayload);

        ContextUtils.storeMAPs(mapCodec.unmarshalMAPs(msgPayload), msgPayload, !msgPayloadInbound);

        msgPayload.put(AbstractHTTPDestination.HTTP_REQUEST, msgPayloadServletReq);

        if (!msgPayloadInbound) {
            msgPayload.put(AbstractHTTPDestination.HTTP_RESPONSE, msgPayloadServletResp);
        }

        // TEMP: dev
        LOGGER.warn(String.format("msgPayload=%s", ReflectionToStringBuilder.toString(msgPayload)));

        for (Header msgPayloadSoapHeader : msgPayload.getHeaders()) {
            LOGGER.warn(String.format("msgPayloadSoapHeader=%s", ReflectionToStringBuilder.toString(msgPayloadSoapHeader)));
        }

        for (Object msgPayloadContentPart : PhizWsUtils.getMessageContents(msgPayload)) {
            LOGGER.warn(String.format("msgPayloadContentPart=%s", ReflectionToStringBuilder.toString(msgPayloadContentPart)));
        }

        return msgPayload;
    }

    @Override
    @SuppressWarnings({ CompilerWarnings.RAWTYPES, CompilerWarnings.UNCHECKED })
    protected Message createMessageInternal(SoapMessage msgPayload, MessageProperties msgProps) throws Exception {
        boolean msgPayloadInbound =
            (msgPayload.containsKey(org.apache.cxf.message.Message.INBOUND_MESSAGE) ? MessageUtils.getContextualBoolean(msgPayload,
                org.apache.cxf.message.Message.INBOUND_MESSAGE, false) : !MessageUtils.isOutbound(msgPayload)), msgPayloadRequestor =
            MessageUtils.isRequestor(msgPayload), msgPayloadResp = (!msgPayloadInbound ^ msgPayloadRequestor);
        byte[] msgPayloadContent;

        if (msgPayload.getContentFormats().contains(Node.class)) {
            Marshaller marshaller = this.dataBinding.getContext().createMarshaller();
            QName soapHeaderQname = this.soapVersion.getHeader(), soapBodyQname = this.soapVersion.getBody();
            Document msgPayloadContentDoc = ((Document) msgPayload.getContent(Node.class));
            Element msgPayloadContentElem = ((Element) msgPayloadContentDoc.getDocumentElement().cloneNode(true)), msgPayloadContentSoapBodyElem =
                ((Element) msgPayloadContentElem.getElementsByTagNameNS(soapBodyQname.getNamespaceURI(), soapBodyQname.getLocalPart()).item(0)), msgPayloadContentPartElem;
            List<MessagePartInfo> msgPayloadContentPartInfos =
                (msgPayloadInbound ? msgPayload.getExchange().getBindingOperationInfo().getInput() : msgPayload.getExchange().getBindingOperationInfo()
                    .getOutput()).getMessageParts();
            MessageContentsList msgPayloadContents = PhizWsUtils.getMessageContents(msgPayload);
            Object msgPayloadContentPart;
            StringResult msgPayloadContentPartResult;

            for (final MessagePartInfo msgPayloadContentPartInfo : msgPayloadContentPartInfos) {
                if (!msgPayloadContents.hasValue(msgPayloadContentPartInfo)) {
                    continue;
                }

                if ((msgPayloadContentPart = msgPayloadContents.get(msgPayloadContentPartInfo)) instanceof Holder<?>) {
                    msgPayloadContentPartElem = msgPayloadContentDoc.createElementNS(soapHeaderQname.getNamespaceURI(), soapHeaderQname.getLocalPart());

                    marshaller.marshal(new JAXBElement(msgPayloadContentPartInfo.getElementQName(), Object.class, msgPayloadContentPart) {
                        private final static long serialVersionUID = 0L;

                        @Override
                        public Class<?> getDeclaredType() {
                            return msgPayloadContentPartInfo.getTypeClass();
                        }
                    }, (msgPayloadContentPartResult = new StringResult()));
                    msgPayloadContentPartElem.appendChild(msgPayloadContentDoc.importNode(
                        StaxUtils.read(new StringSource(msgPayloadContentPartResult.toString())).getDocumentElement(), true));

                    msgPayloadContentSoapBodyElem.insertBefore(msgPayloadContentPartElem, msgPayloadContentSoapBodyElem);
                } else if (msgPayloadContentPart != null) {
                    marshaller.marshal(new JAXBElement(msgPayloadContentPartInfo.getElementQName(), Object.class, msgPayloadContentPart) {
                        private final static long serialVersionUID = 0L;

                        @Override
                        public Class<?> getDeclaredType() {
                            return msgPayloadContentPartInfo.getTypeClass();
                        }
                    }, (msgPayloadContentPartResult = new StringResult()));
                    msgPayloadContentSoapBodyElem.appendChild(msgPayloadContentDoc.importNode(
                        StaxUtils.read(new StringSource(msgPayloadContentPartResult.toString())).getDocumentElement(), true));
                }
            }

            msgPayloadContent = StaxUtils.toString(msgPayloadContentElem).getBytes(this.charsetEnc);
        } else {
            BufferedInputStream msgBufferedInStream = PhizWsUtils.getMarkedInputStream(msgPayload);

            try {
                msgPayloadContent = IOUtils.toByteArray(msgBufferedInStream);
            } finally {
                msgBufferedInStream.reset();
            }
        }

        msgProps.setContentEncoding(((String) msgPayload.get(org.apache.cxf.message.Message.ENCODING)));
        msgProps.setContentLength(msgPayloadContent.length);
        msgProps.setContentType(((String) msgPayload.get(org.apache.cxf.message.Message.CONTENT_TYPE)));
        msgProps.setCorrelationId(PhizWsAddressingUtils.getAddressingMessageId(msgPayload).getBytes(this.charsetEnc));
        msgProps.setMessageId(msgPayload.getId());
        msgProps.setTimestamp(new Date());

        msgProps.setHeader(WS_SERVICE_NAME_MSG_PROP_NAME, msgPayload.get(org.apache.cxf.message.Message.WSDL_SERVICE).toString());
        msgProps.setHeader(WS_OP_NAME_MSG_PROP_NAME, msgPayload.get(org.apache.cxf.message.Message.WSDL_OPERATION).toString());

        HttpServletRequest msgPayloadServletReq = PhizWsUtils.getHttpServletRequest(msgPayload);
        HttpHeaders msgPayloadHttpHeaders;

        // noinspection ConstantConditions
        msgProps.setHeader(HTTP_AUTH_TYPE_MSG_PROP_NAME, msgPayloadServletReq.getAuthType());
        msgProps.setHeader(HTTP_CONTEXT_PATH_MSG_PROP_NAME, msgPayloadServletReq.getContextPath());
        msgProps.setHeader(HTTP_LOCAL_NAME_MSG_PROP_NAME, msgPayloadServletReq.getLocalName());
        msgProps.setHeader(HTTP_LOCAL_PORT_MSG_PROP_NAME, msgPayloadServletReq.getLocalPort());
        msgProps.setHeader(HTTP_METHOD_MSG_PROP_NAME, msgPayloadServletReq.getMethod());
        msgProps.setHeader(HTTP_PATH_INFO_MSG_PROP_NAME, msgPayloadServletReq.getPathInfo());
        msgProps.setHeader(HTTP_PROTOCOL_MSG_PROP_NAME, msgPayloadServletReq.getProtocol());
        msgProps.setHeader(HTTP_QUERY_MSG_PROP_NAME, msgPayloadServletReq.getQueryString());
        msgProps.setHeader(HTTP_REMOTE_ADDR_MSG_PROP_NAME, msgPayloadServletReq.getRemoteAddr());
        msgProps.setHeader(HTTP_REMOTE_PORT_MSG_PROP_NAME, msgPayloadServletReq.getRemotePort());
        msgProps.setHeader(HTTP_SCHEME_MSG_PROP_NAME, msgPayloadServletReq.getScheme());
        msgProps.setHeader(HTTP_SERVER_NAME_MSG_PROP_NAME, msgPayloadServletReq.getServerName());
        msgProps.setHeader(HTTP_SERVER_PORT_MSG_PROP_NAME, msgPayloadServletReq.getServerPort());
        msgProps.setHeader(HTTP_SERVLET_PATH_MSG_PROP_NAME, msgPayloadServletReq.getServletPath());

        Principal msgPayloadServletReqUserPrincipal = msgPayloadServletReq.getUserPrincipal();

        if (msgPayloadServletReqUserPrincipal != null) {
            msgProps.setHeader(HTTP_USER_PRINCIPAL_MSG_PROP_NAME, msgPayloadServletReqUserPrincipal.getName());
        }

        msgProps.setHeader(WS_MSG_INBOUND_MSG_PROP_NAME, msgPayloadInbound);
        msgProps.setHeader(WS_MSG_REQ_ROLE_MSG_PROP_NAME, msgPayloadRequestor);
        msgPayloadHttpHeaders = PhizServletUtils.getHeaders(msgPayloadServletReq);

        if (!msgPayloadInbound) {
            HttpServletResponse msgPayloadServletResp = PhizWsUtils.getHttpServletResponse(msgPayload);

            msgPayloadHttpHeaders = PhizServletUtils.getHeaders(msgPayloadServletResp);

            // noinspection ConstantConditions
            msgProps.setHeader(HTTP_STATUS_MSG_PROP_NAME, msgPayloadServletResp.getStatus());
        }

        msgPayloadHttpHeaders.toSingleValueMap().forEach(
            (headerName, headerValue) -> msgProps.setHeader((HTTP_HEADER_MSG_PROP_NAME_PREFIX + headerName), headerValue));

        // TEMP: dev
        LOGGER.warn(String.format("createMsgPayloadContent=%s", IOUtils.toString(msgPayloadContent, this.charsetEnc.name())));
        LOGGER.warn(String.format("createMsgProps=%s", StringUtils.join(msgProps, "\n")));

        return new Message(msgPayloadContent, msgProps);
    }
}
