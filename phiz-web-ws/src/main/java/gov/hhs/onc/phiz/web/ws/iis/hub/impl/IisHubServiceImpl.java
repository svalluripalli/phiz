package gov.hhs.onc.phiz.web.ws.iis.hub.impl;

import gov.hhs.onc.phiz.destination.PhizDestination;
import gov.hhs.onc.phiz.destination.PhizDestinationRegistry;
import gov.hhs.onc.phiz.web.ws.iis.hub.IisHubHttpHeaders;
import gov.hhs.onc.phiz.web.ws.iis.hub.IisHubService;
import gov.hhs.onc.phiz.web.ws.iis.impl.AbstractIisService;
import gov.hhs.onc.phiz.web.ws.jaxws.impl.PhizJaxWsClientProxyFactoryBean;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsUtils;
import gov.hhs.onc.phiz.ws.PhizWsAddressingActions;
import gov.hhs.onc.phiz.ws.PhizWsNames;
import gov.hhs.onc.phiz.ws.iis.IisPortType;
import gov.hhs.onc.phiz.ws.iis.MessageTooLargeFault;
import gov.hhs.onc.phiz.ws.iis.SecurityFault;
import gov.hhs.onc.phiz.ws.iis.SubmitSingleMessageRequestType;
import gov.hhs.onc.phiz.ws.iis.SubmitSingleMessageResponseType;
import gov.hhs.onc.phiz.ws.iis.hub.DestinationConnectionFault;
import gov.hhs.onc.phiz.ws.iis.hub.HubClientFault;
import gov.hhs.onc.phiz.ws.iis.hub.HubRequestHeaderType;
import gov.hhs.onc.phiz.ws.iis.hub.HubResponseHeaderType;
import gov.hhs.onc.phiz.ws.iis.hub.IisHubPortType;
import gov.hhs.onc.phiz.ws.iis.hub.UnknownDestinationFault;
import gov.hhs.onc.phiz.ws.iis.hub.impl.DestinationConnectionFaultTypeImpl;
import gov.hhs.onc.phiz.ws.iis.hub.impl.HubClientFaultTypeImpl;
import gov.hhs.onc.phiz.ws.iis.hub.impl.HubResponseHeaderTypeImpl;
import gov.hhs.onc.phiz.ws.iis.hub.impl.ObjectFactory;
import gov.hhs.onc.phiz.ws.iis.hub.impl.UnknownDestinationFaultTypeImpl;
import gov.hhs.onc.phiz.xml.PhizXmlNs;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.Holder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.Headers;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.ws.addressing.Names;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings({ "ValidExternallyBoundObject" })
@WebService(portName = PhizWsNames.PORT_HUB, serviceName = PhizWsNames.SERVICE_HUB, targetNamespace = PhizXmlNs.IIS_HUB)
public class IisHubServiceImpl extends AbstractIisService implements IisHubPortType, IisHubService {
    @Autowired
    private ObjectFactory hubObjFactory;

    @Autowired
    private PhizDestinationRegistry destReg;

    private String clientProxyFactoryBeanName;

    public IisHubServiceImpl(String clientProxyFactoryBeanName) {
        this.clientProxyFactoryBeanName = clientProxyFactoryBeanName;
    }

    @Override
    public void submitSingleMessage(SubmitSingleMessageRequestType reqParams, HubRequestHeaderType hubReqHeader,
        Holder<SubmitSingleMessageResponseType> respParams, Holder<HubResponseHeaderType> hubRespHeader) throws DestinationConnectionFault, HubClientFault,
        MessageTooLargeFault, SecurityFault, UnknownDestinationFault {
        Pair<SubmitSingleMessageResponseType, HubResponseHeaderType> respPair = this.submitSingleMessageInternal(reqParams, hubReqHeader);

        respParams.value = respPair.getLeft();
        hubRespHeader.value = respPair.getRight();
    }

    private Pair<SubmitSingleMessageResponseType, HubResponseHeaderType> submitSingleMessageInternal(SubmitSingleMessageRequestType reqParams,
        HubRequestHeaderType hubReqHeader) throws DestinationConnectionFault, HubClientFault, MessageTooLargeFault, SecurityFault, UnknownDestinationFault {
        String destId = hubReqHeader.getDestinationId();
        PhizDestination dest = this.destReg.findById(destId);

        if (dest == null) {
            throw new UnknownDestinationFault("IIS destination ID is not registered.", new UnknownDestinationFaultTypeImpl(destId));
        }

        WrappedMessageContext reqMsgContext = PhizWsUtils.getMessageContext(this.wsContext);
        SoapMessage reqMsg = ((SoapMessage) reqMsgContext.getWrappedMessage());
        HttpServletRequest servletReq = PhizWsUtils.getHttpServletRequest(reqMsgContext);
        URI destUri = dest.getUri();
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

        Map<String, List<String>> clientReqHttpHeaders =
            Headers
                .getSetProtocolHeaders(reqMsg)
                .entrySet()
                .stream()
                .filter(
                    ((Entry<String, List<String>> reqHttpHeaderEntry) -> StringUtils.startsWithIgnoreCase(reqHttpHeaderEntry.getKey(), IisHubHttpHeaders.PREFIX)))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        clientProxyFactoryBean.getOutInterceptors().add(new AbstractPhaseInterceptor<Message>(Phase.PRE_STREAM) {
            @Override
            public void handleMessage(Message iisClientReqMsg) throws Fault {
                Headers.getSetProtocolHeaders(iisClientReqMsg).putAll(clientReqHttpHeaders);
            }
        });

        IisPortType clientPort;

        try {
            clientPort = ((IisPortType) clientProxyFactoryBean.getObject());
        } catch (Exception e) {
            throw new HubClientFault("Unable to build IIS destination web service client.", new HubClientFaultTypeImpl(destId, destUriStr), e);
        }

        Client client = ClientProxy.getClient(clientPort);

        AddressingProperties clientReqAddrProps = new AddressingProperties(Names.WSA_NAMESPACE_NAME);
        clientReqAddrProps.setAction(ContextUtils.getAttributedURI(PhizWsAddressingActions.SUBMIT_SINGLE_MSG_REQ));
        clientReqAddrProps.setMessageID(ContextUtils.retrieveMAPs(reqMsg, false, false).getMessageID());
        client.getRequestContext().put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, clientReqAddrProps);

        return new ImmutablePair<>(clientPort.submitSingleMessage(reqParams), new HubResponseHeaderTypeImpl(destId, destUriStr));
    }
}
