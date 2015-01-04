package gov.hhs.onc.phiz.data.queue.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.MessageImpl;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

@Component("msgConvSoap")
public class SoapMessageConverter extends AbstractPhizMessageConverter<SoapMessage> {
    public SoapMessageConverter() {
        super(SoapMessage.class);
    }

    @Override
    protected SoapMessage fromMessageInternal(Message msg) throws Exception {
        org.apache.cxf.message.Message msgInnerPayload = new MessageImpl();
        msgInnerPayload.setContent(InputStream.class, new ByteArrayInputStream(msg.getBody()));

        return new SoapMessage(msgInnerPayload);
    }

    @Override
    protected Message createMessageInternal(SoapMessage msgPayload, MessageProperties msgProps) throws Exception {
        InputStream msgInStream = new BufferedInputStream(msgPayload.getContent(InputStream.class));
        msgInStream.mark(msgInStream.available());
        msgPayload.setContent(InputStream.class, msgInStream);
        
        try {
            return new Message(IOUtils.toByteArray(msgInStream), msgProps);
        } finally {
            msgInStream.reset();
        }
    }
}
