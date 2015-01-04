package gov.hhs.onc.phiz.data.queue.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

public abstract class AbstractPhizMessageConverter<T> extends AbstractMessageConverter {
    protected Class<T> msgPayloadClass;

    protected AbstractPhizMessageConverter(Class<T> msgPayloadClass) {
        this.msgPayloadClass = msgPayloadClass;
    }

    @Override
    public Object fromMessage(Message msg) throws MessageConversionException {
        try {
            return this.fromMessageInternal(msg);
        } catch (Exception e) {
            throw new MessageConversionException(String.format("Unable to process message (payloadClass=%s, props={%s}).", this.msgPayloadClass.getName(),
                StringUtils.removeEnd(StringUtils.split(msg.getMessageProperties().toString(), "[", 2)[1], "]")), e);
        }
    }

    @Override
    protected Message createMessage(Object msgPayload, MessageProperties msgProps) {
        try {
            return this.createMessageInternal(this.msgPayloadClass.cast(msgPayload), msgProps);
        } catch (Exception e) {
            throw new MessageConversionException(
                String.format("Unable to create message (payloadClass=%s, payloadObjClass=%s, props={%s}).", this.msgPayloadClass.getName(), msgPayload
                    .getClass().getName(), StringUtils.removeEnd(StringUtils.split(msgProps.toString(), "[", 2)[1], "]")), e);
        }
    }

    protected abstract T fromMessageInternal(Message msg) throws Exception;

    protected abstract Message createMessageInternal(T msgPayload, MessageProperties msgProps) throws Exception;
}
