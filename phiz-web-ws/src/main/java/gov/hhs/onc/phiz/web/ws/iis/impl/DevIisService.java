package gov.hhs.onc.phiz.web.ws.iis.impl;

import gov.hhs.onc.phiz.web.ws.iis.IisService;
import gov.hhs.onc.phiz.ws.PhizWsNames;
import gov.hhs.onc.phiz.ws.iis.IisPortType;
import gov.hhs.onc.phiz.ws.iis.MessageTooLargeFault;
import gov.hhs.onc.phiz.ws.iis.SecurityFault;
import gov.hhs.onc.phiz.ws.iis.SubmitSingleMessageRequestType;
import gov.hhs.onc.phiz.ws.iis.SubmitSingleMessageResponseType;
import gov.hhs.onc.phiz.ws.iis.impl.SubmitSingleMessageResponseTypeImpl;
import gov.hhs.onc.phiz.xml.PhizXmlNs;
import javax.jws.WebService;

@WebService(portName = PhizWsNames.PORT, serviceName = PhizWsNames.SERVICE, targetNamespace = PhizXmlNs.IIS)
public class DevIisService extends AbstractIisService implements IisPortType, IisService {
    @Override
    @SuppressWarnings({ "ValidExternallyBoundObject" })
    public SubmitSingleMessageResponseType submitSingleMessage(SubmitSingleMessageRequestType reqParams) throws MessageTooLargeFault, SecurityFault {
        return new SubmitSingleMessageResponseTypeImpl(reqParams.getHl7Message());
    }
}
