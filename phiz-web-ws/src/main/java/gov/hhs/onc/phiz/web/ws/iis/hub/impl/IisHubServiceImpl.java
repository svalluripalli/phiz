package gov.hhs.onc.phiz.web.ws.iis.hub.impl;

import gov.hhs.onc.phiz.destination.PhizDestination;
import gov.hhs.onc.phiz.destination.PhizDestinationRegistry;
import gov.hhs.onc.phiz.web.ws.PhizWsHttpHeaders;
import gov.hhs.onc.phiz.web.ws.feature.impl.PhizLoggingFeature;
import gov.hhs.onc.phiz.web.ws.iis.hub.IisHubService;
import gov.hhs.onc.phiz.web.ws.iis.impl.AbstractIisService;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsUtils;
import gov.hhs.onc.phiz.ws.PhizWsAddressingActions;
import gov.hhs.onc.phiz.ws.PhizWsNames;
import gov.hhs.onc.phiz.ws.PhizWsQnames;
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
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.Holder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
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

    private String clientBeanName;

    public IisHubServiceImpl(String clientBeanName) {
        this.clientBeanName = clientBeanName;
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
        HttpServletRequest servletReq = PhizWsUtils.getProperty(reqMsgContext, AbstractHTTPDestination.HTTP_REQUEST, HttpServletRequest.class);
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

        Client client = ((Client) this.appContext.getBean(this.clientBeanName, destUriStr));
        Map<String, Object> clientReqContext = client.getRequestContext();

        clientReqContext.put(
            Message.PROTOCOL_HEADERS,
            Headers
                .getSetProtocolHeaders(reqMsg)
                .entrySet()
                .stream()
                .filter(
                    ((Entry<String, List<String>> reqHttpHeaderEntry) -> StringUtils.startsWithIgnoreCase(reqHttpHeaderEntry.getKey(),
                        PhizWsHttpHeaders.EXT_IIS_HUB_PREFIX))).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

        AddressingProperties clientReqAddrProps = new AddressingProperties(Names.WSA_NAMESPACE_NAME);
        clientReqAddrProps.setAction(ContextUtils.getAttributedURI(PhizWsAddressingActions.SUBMIT_SINGLE_MSG_REQ));
        clientReqAddrProps.setMessageID(ContextUtils.retrieveMAPs(reqMsg, false, false).getMessageID());
        clientReqContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, clientReqAddrProps);

        Exchange clientExchange = new ExchangeImpl();
        clientExchange.put(PhizLoggingFeature.WS_MSG_EVENT_ID_PROP_NAME, reqMsg.getExchange().get(PhizLoggingFeature.WS_MSG_EVENT_ID_PROP_NAME));

        try {
            return new ImmutablePair<>(((SubmitSingleMessageResponseType) client.invoke(
                client.getEndpoint().getBinding().getBindingInfo().getOperation(PhizWsQnames.SUBMIT_SINGLE_MSG_OP), new Object[] { reqParams }, null,
                clientExchange)[0]), new HubResponseHeaderTypeImpl(destId, destUriStr));
        } catch (DestinationConnectionFault | HubClientFault | MessageTooLargeFault | SecurityFault | UnknownDestinationFault e) {
            throw e;
        } catch (Exception e) {
            SocketTimeoutException clientReqTimeoutException =
                ((SocketTimeoutException) Stream.of(ExceptionUtils.getThrowables(e))
                    .filter(clientReqExceptionCause -> (clientReqExceptionCause instanceof SocketTimeoutException)).findFirst().orElse(null));

            if (clientReqTimeoutException != null) {
                throw new DestinationConnectionFault("Connection to IIS destination web service timed out.", new DestinationConnectionFaultTypeImpl(destId,
                    destUriStr), clientReqTimeoutException);
            } else {
                throw new HubClientFault("Unable to invoke IIS destination web service.", new HubClientFaultTypeImpl(destId, destUriStr), e);
            }
        }
    }
}
