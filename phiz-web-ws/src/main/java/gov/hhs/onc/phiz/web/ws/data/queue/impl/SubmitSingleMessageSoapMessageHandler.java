package gov.hhs.onc.phiz.web.ws.data.queue.impl;

import gov.hhs.onc.phiz.data.queue.impl.AbstractPhizMessageHandler;
import org.apache.cxf.binding.soap.SoapMessage;

public class SubmitSingleMessageSoapMessageHandler extends AbstractPhizMessageHandler<SoapMessage, SoapMessage> {
    @Override
    public SoapMessage handleMessage(SoapMessage reqMsg) {
        return reqMsg;
    }
}
