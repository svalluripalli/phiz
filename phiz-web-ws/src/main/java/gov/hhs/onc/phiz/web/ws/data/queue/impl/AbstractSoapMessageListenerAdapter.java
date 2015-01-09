package gov.hhs.onc.phiz.web.ws.data.queue.impl;

import gov.hhs.onc.phiz.data.queue.impl.AbstractPhizMessageListenerAdapter;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.MessageContentsList;

public abstract class AbstractSoapMessageListenerAdapter extends AbstractPhizMessageListenerAdapter<SoapMessage, SoapMessage> {
    protected AbstractSoapMessageListenerAdapter() {
        super(SoapMessage.class, SoapMessage.class);
    }

    @Override
    protected Object[] buildListenerArguments(Object msgPayloadObj) {
        SoapMessage msgPayload = ((SoapMessage) msgPayloadObj);
        MessageContentsList msgPayloadContents = PhizWsUtils.getMessageContents(msgPayload);

        List<Object> msgListenerArgs = new ArrayList<>(msgPayloadContents.size() + 1);
        msgListenerArgs.add(msgPayload);
        msgListenerArgs.addAll(msgPayloadContents);

        return msgListenerArgs.toArray();
    }
}
