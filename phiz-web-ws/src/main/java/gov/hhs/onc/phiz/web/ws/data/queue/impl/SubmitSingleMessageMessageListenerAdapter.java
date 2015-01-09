package gov.hhs.onc.phiz.web.ws.data.queue.impl;

import gov.hhs.onc.phiz.web.ws.iis.hub.IisHubHttpHeaders;
import gov.hhs.onc.phiz.web.ws.jaxws.impl.PhizJaxWsClientProxyFactoryBean;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsUtils;
import gov.hhs.onc.phiz.ws.PhizWsAddressingActions;
import gov.hhs.onc.phiz.ws.iis.IisPortType;
import gov.hhs.onc.phiz.ws.iis.SubmitSingleMessageRequestType;
import gov.hhs.onc.phiz.ws.iis.hub.DestinationConnectionFault;
import gov.hhs.onc.phiz.ws.iis.hub.HubClientFault;
import gov.hhs.onc.phiz.ws.iis.hub.HubRequestHeaderType;
import gov.hhs.onc.phiz.ws.iis.hub.impl.DestinationConnectionFaultTypeImpl;
import gov.hhs.onc.phiz.ws.iis.hub.impl.HubClientFaultTypeImpl;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.Holder;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.Headers;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.ws.addressing.Names;
import org.springframework.beans.factory.annotation.Value;

public class SubmitSingleMessageMessageListenerAdapter extends AbstractSoapMessageListenerAdapter {
    @Value("${phiz.ws.iis.dev.dest.uri}")
    private String devIisDestUriStr;

    private String clientProxyFactoryBeanName;

    private ThreadLocal<Holder<SoapMessage>> clientReqMsgHolderThreadLocal = new ThreadLocal<Holder<SoapMessage>>() {
        @Override
        protected Holder<SoapMessage> initialValue() {
            return new Holder<>();
        }
    };

    public SubmitSingleMessageMessageListenerAdapter(String clientProxyFactoryBeanName) {
        super();

        this.clientProxyFactoryBeanName = clientProxyFactoryBeanName;
    }

    public SoapMessage handleMessage(SoapMessage reqMsg, SubmitSingleMessageRequestType reqParams, HubRequestHeaderType hubReqHeader) throws Exception {
        this.handleMessageInternal(reqMsg, reqParams, hubReqHeader);

        try {
            SoapMessage clientReqMsg = this.clientReqMsgHolderThreadLocal.get().value;
            Exchange clientMsgExchange = clientReqMsg.getExchange();

            SoapMessage clientRespMsg =
                ((SoapMessage) Optional.ofNullable(((clientMsgExchange != null) ? clientMsgExchange.getInMessage() : null)).orElse(clientReqMsg));
            clientRespMsg.put(Message.INBOUND_MESSAGE, false);
            clientRespMsg.put(AbstractHTTPDestination.HTTP_REQUEST, clientReqMsg.get((AbstractHTTPDestination.HTTP_REQUEST + ".hold")));

            return clientRespMsg;
        } finally {
            this.clientReqMsgHolderThreadLocal.remove();
        }
    }

    private void handleMessageInternal(SoapMessage reqMsg, SubmitSingleMessageRequestType reqParams, HubRequestHeaderType hubReqHeader) throws Exception {
        HttpServletRequest servletReq = PhizWsUtils.getHttpServletRequest(reqMsg);
        String destId = hubReqHeader.getDestinationId();
        URI destUri = new URI(this.devIisDestUriStr);
        String destUriStr = destUri.toString();

        if (!destUri.isAbsolute()) {
            try {
                // noinspection ConstantConditions
                destUriStr =
                    (destUri =
                        destUri.relativize(new URI(servletReq.getScheme(), null, servletReq.getServerName(), servletReq.getServerPort(), StringUtils
                            .prependIfMissing(destUri.getPath(), servletReq.getContextPath()), null, null))).toString();
            } catch (URISyntaxException e) {
                throw new DestinationConnectionFault("Unable to relativize local IIS destination URI.", new DestinationConnectionFaultTypeImpl(destId,
                    destUriStr), e);
            }
        }

        PhizJaxWsClientProxyFactoryBean clientProxyFactoryBean =
            this.appContext.getBean(this.clientProxyFactoryBeanName, PhizJaxWsClientProxyFactoryBean.class);
        clientProxyFactoryBean.setAddress(destUriStr);

        final Holder<SoapMessage> clientReqMsgHolder = clientReqMsgHolderThreadLocal.get();
        Map<String, List<String>> clientReqHttpHeaders =
            Headers.getSetProtocolHeaders(reqMsg).entrySet().stream()
                .filter(((Entry<String, List<String>> httpReqHeaderEntry) -> httpReqHeaderEntry.getKey().equalsIgnoreCase(IisHubHttpHeaders.DEV_ACTION_NAME)))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        clientProxyFactoryBean.getOutInterceptors().add(new AbstractPhaseInterceptor<Message>(Phase.PRE_STREAM) {
            @Override
            public void handleMessage(Message clientReqMsg) throws Fault {
                clientReqMsgHolder.value = ((SoapMessage) clientReqMsg);

                Headers.getSetProtocolHeaders(clientReqMsg).putAll(clientReqHttpHeaders);
                
                clientReqMsg.put((AbstractHTTPDestination.HTTP_REQUEST + ".hold"), clientReqMsg.get(AbstractHTTPDestination.HTTP_REQUEST));
            }
        });

        IisPortType clientPort;

        try {
            clientPort = ((IisPortType) clientProxyFactoryBean.getObject());
        } catch (Exception e) {
            throw new HubClientFault("Unable to build IIS destination web service client.", new HubClientFaultTypeImpl(destId, destUriStr), e);
        }

        Client client = ClientProxy.getClient(clientPort);
        client.setThreadLocalRequestContext(true);

        AddressingProperties clientReqAddrProps = new AddressingProperties(Names.WSA_NAMESPACE_NAME);
        clientReqAddrProps.setAction(ContextUtils.getAttributedURI(PhizWsAddressingActions.SUBMIT_SINGLE_MSG_REQ));
        clientReqAddrProps.setMessageID(ContextUtils.retrieveMAPs(reqMsg, false, false).getMessageID());
        client.getRequestContext().put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, clientReqAddrProps);

        clientPort.submitSingleMessage(reqParams);
    }
}
