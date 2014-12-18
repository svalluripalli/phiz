package gov.hhs.onc.phiz.web.ws.hub.impl;

import gov.hhs.onc.phiz.web.ws.hub.IisHubService;
import gov.hhs.onc.phiz.web.ws.impl.AbstractIisService;
import gov.hhs.onc.phiz.ws.MessageTooLargeFault;
import gov.hhs.onc.phiz.ws.PhizWsNames;
import gov.hhs.onc.phiz.ws.SecurityFault;
import gov.hhs.onc.phiz.ws.SubmitSingleMessageRequestType;
import gov.hhs.onc.phiz.ws.SubmitSingleMessageResponseType;
import gov.hhs.onc.phiz.ws.hub.DestinationConnectionFault;
import gov.hhs.onc.phiz.ws.hub.HubClientFault;
import gov.hhs.onc.phiz.ws.hub.HubRequestHeaderType;
import gov.hhs.onc.phiz.ws.hub.HubResponseHeaderType;
import gov.hhs.onc.phiz.ws.hub.IisHubPortType;
import gov.hhs.onc.phiz.ws.hub.UnknownDestinationFault;
import gov.hhs.onc.phiz.ws.hub.impl.ObjectFactory;
import gov.hhs.onc.phiz.xml.PhizXmlNs;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

@WebService(portName = PhizWsNames.PORT_HUB, serviceName = PhizWsNames.SERVICE_HUB, targetNamespace = PhizXmlNs.IIS_HUB)
public class IisHubServiceImpl extends AbstractIisService implements IisHubPortType, IisHubService {
    @Autowired
    private ObjectFactory hubObjFactory;

    @Override
    @SuppressWarnings({ "ValidExternallyBoundObject" })
    public void submitSingleMessage(SubmitSingleMessageRequestType reqParams, HubRequestHeaderType hubReqHeader,
        Holder<SubmitSingleMessageResponseType> respParams, Holder<HubResponseHeaderType> hubRespHeader) throws DestinationConnectionFault, HubClientFault,
        MessageTooLargeFault, SecurityFault, UnknownDestinationFault {
        Pair<SubmitSingleMessageResponseType, HubResponseHeaderType> respPair = this.submitSingleMessageInternal(reqParams, hubReqHeader);

        respParams.value = respPair.getLeft();
        hubRespHeader.value = respPair.getRight();
    }

    private Pair<SubmitSingleMessageResponseType, HubResponseHeaderType> submitSingleMessageInternal(SubmitSingleMessageRequestType reqParams,
        HubRequestHeaderType hubReqHeader) throws DestinationConnectionFault, HubClientFault, MessageTooLargeFault, SecurityFault, UnknownDestinationFault {
        // TODO: Remove placeholder implementation.
        SubmitSingleMessageResponseType respParams = this.objFactory.createSubmitSingleMessageResponseType();
        respParams.setHl7Message(reqParams.getHl7Message());

        HubResponseHeaderType hubRespHeader = this.hubObjFactory.createHubResponseHeaderType();
        hubRespHeader.setDestinationId(hubReqHeader.getDestinationId());
        hubRespHeader.setDestinationUri("about:blank");

        return new ImmutablePair<>(respParams, hubRespHeader);
    }
}
