package gov.hhs.onc.phiz.web.ws.jaxws.impl;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.spring.JaxWsProxyFactoryBeanDefinitionParser.JAXWSSpringClientProxyFactoryBean;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class PhizJaxWsClientProxyFactoryBean extends JAXWSSpringClientProxyFactoryBean implements SmartFactoryBean<Object> {
    @Autowired
    public PhizJaxWsClientProxyFactoryBean(@Value("#{ busPhiz }") Bus bus) {
        super();
        
        this.setBus(bus);
    }
    
    @Override
    public boolean isEagerInit() {
        return false;
    }

    @Override
    public boolean isPrototype() {
        return true;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
