package gov.hhs.onc.phiz.web.ws.iis.hub.impl;

import gov.hhs.onc.phiz.web.ws.iis.hub.IisHubService;
import gov.hhs.onc.phiz.web.ws.iis.impl.AbstractIisService;
import gov.hhs.onc.phiz.ws.iis.ConnectivityTestRequestType;
import gov.hhs.onc.phiz.ws.iis.ConnectivityTestResponseType;
import gov.hhs.onc.phiz.ws.iis.MessageTooLargeFault;
import gov.hhs.onc.phiz.ws.PhizWsNames;
import gov.hhs.onc.phiz.ws.iis.SecurityFault;
import gov.hhs.onc.phiz.ws.iis.SubmitSingleMessageRequestType;
import gov.hhs.onc.phiz.ws.iis.SubmitSingleMessageResponseType;
import gov.hhs.onc.phiz.ws.iis.hub.DestinationConnectionFault;
import gov.hhs.onc.phiz.ws.iis.hub.HubClientFault;
import gov.hhs.onc.phiz.ws.iis.hub.HubRequestHeaderType;
import gov.hhs.onc.phiz.ws.iis.hub.HubResponseHeaderType;
import gov.hhs.onc.phiz.ws.iis.hub.IisHubPortType;
import gov.hhs.onc.phiz.ws.iis.hub.UnknownDestinationFault;
import gov.hhs.onc.phiz.ws.iis.hub.impl.ObjectFactory;
import gov.hhs.onc.phiz.xml.PhizXmlNs;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.springframework.amqp.core.Address;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@WebService(portName = PhizWsNames.PORT_HUB, serviceName = PhizWsNames.SERVICE_HUB, targetNamespace = PhizXmlNs.IIS_HUB)
public class IisHubServiceImpl extends AbstractIisService implements IisHubPortType, IisHubService {
    private ObjectFactory hubObjFactory;
    private RabbitTemplate rabbitTemplate;
    private Address reqMsgAddr;

    @Override
    @SuppressWarnings({ "ValidExternallyBoundObject" })
    public void submitSingleMessage(SubmitSingleMessageRequestType reqParams, HubRequestHeaderType hubReqHeader,
        Holder<SubmitSingleMessageResponseType> respParams, Holder<HubResponseHeaderType> hubRespHeader) throws DestinationConnectionFault, HubClientFault,
        MessageTooLargeFault, SecurityFault, UnknownDestinationFault {
        Pair<SubmitSingleMessageResponseType, HubResponseHeaderType> respPair = this.submitSingleMessageInternal(reqParams, hubReqHeader);

        respParams.value = respPair.getLeft();
        hubRespHeader.value = respPair.getRight();
    }

    @Override
    protected ConnectivityTestResponseType connectivityTestInternal(ConnectivityTestRequestType reqParams) {
        // @formatter:off
        /*
        // TEMP: dev
        this.rabbitTemplate.convertSendAndReceive(this.reqMsgAddr.getExchangeName(), this.reqMsgAddr.getRoutingKey(),
            ((WrappedMessageContext) this.wsContext.getMessageContext()).getWrappedMessage());
        */
        // @formatter:on
        
        return super.connectivityTestInternal(reqParams);
    }

    private Pair<SubmitSingleMessageResponseType, HubResponseHeaderType> submitSingleMessageInternal(SubmitSingleMessageRequestType reqParams,
        HubRequestHeaderType hubReqHeader) throws DestinationConnectionFault, HubClientFault, MessageTooLargeFault, SecurityFault, UnknownDestinationFault {
        // @formatter:off
        /*
        // TEMP: dev
        this.rabbitTemplate.convertSendAndReceive(this.reqMsgAddr.getExchangeName(), this.reqMsgAddr.getRoutingKey(),
            ((WrappedMessageContext) this.wsContext.getMessageContext()).getWrappedMessage());
        */
        // @formatter:on
        
        // TODO: Remove placeholder implementation.
        SubmitSingleMessageResponseType respParams = this.objFactory.createSubmitSingleMessageResponseType();
        respParams.setHl7Message(reqParams.getHl7Message());

        HubResponseHeaderType hubRespHeader = this.hubObjFactory.createHubResponseHeaderType();
        hubRespHeader.setDestinationId(hubReqHeader.getDestinationId());
        hubRespHeader.setDestinationUri("about:blank");

        return new ImmutablePair<>(respParams, hubRespHeader);
    }

    @Override
    public ObjectFactory getHubObjectFactory() {
        return this.hubObjFactory;
    }

    @Override
    public void setHubObjectFactory(ObjectFactory hubObjFactory) {
        this.hubObjFactory = hubObjFactory;
    }

    public RabbitTemplate getRabbitTemplate() {
        return this.rabbitTemplate;
    }

    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public Address getRequestMessageAddress() {
        return this.reqMsgAddr;
    }

    public void setRequestMessageAddress(Address reqMsgAddr) {
        this.reqMsgAddr = reqMsgAddr;
    }
}
