package gov.hhs.onc.phiz.web.ws.hub;

import gov.hhs.onc.phiz.web.ws.IisService;
import gov.hhs.onc.phiz.ws.MessageTooLargeFault;
import gov.hhs.onc.phiz.ws.SecurityFault;
import gov.hhs.onc.phiz.ws.SubmitSingleMessageRequestType;
import gov.hhs.onc.phiz.ws.SubmitSingleMessageResponseType;
import gov.hhs.onc.phiz.ws.hub.DestinationConnectionFault;
import gov.hhs.onc.phiz.ws.hub.HubClientFault;
import gov.hhs.onc.phiz.ws.hub.HubRequestHeaderType;
import gov.hhs.onc.phiz.ws.hub.HubResponseHeaderType;
import gov.hhs.onc.phiz.ws.hub.UnknownDestinationFault;
import javax.xml.ws.Holder;

public interface IisHubService extends IisService {
    public void submitSingleMessage(SubmitSingleMessageRequestType reqParams, HubRequestHeaderType hubReqHeader,
        Holder<SubmitSingleMessageResponseType> respParams, Holder<HubResponseHeaderType> hubRespHeader) throws DestinationConnectionFault, HubClientFault,
        MessageTooLargeFault, SecurityFault, UnknownDestinationFault;
}
