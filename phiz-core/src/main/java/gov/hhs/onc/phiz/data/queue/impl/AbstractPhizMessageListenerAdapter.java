package gov.hhs.onc.phiz.data.queue.impl;

import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;

public abstract class AbstractPhizMessageListenerAdapter<T, U> extends MessageListenerAdapter implements ApplicationContextAware {
    protected AbstractApplicationContext appContext;
    protected Class<T> reqMsgPayloadClass;
    protected Class<U> respMsgPayloadClass;

    protected AbstractPhizMessageListenerAdapter(Class<T> reqMsgPayloadClass, Class<U> respMsgPayloadClass) {
        this.reqMsgPayloadClass = reqMsgPayloadClass;
        this.respMsgPayloadClass = respMsgPayloadClass;
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.appContext = ((AbstractApplicationContext) appContext);
    }
}
