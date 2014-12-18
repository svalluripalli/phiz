package gov.hhs.onc.phiz.web.ws.jaxws.impl;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartFactoryBean;

public class PhizJaxWsServerFactoryBean extends JaxWsServerFactoryBean implements DisposableBean, InitializingBean, SmartFactoryBean<Server> {
    @Override
    public Server getObject() throws Exception {
        return this.create();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.init();
    }

    @Override
    public boolean isEagerInit() {
        return false;
    }

    @Override
    public Class<?> getObjectType() {
        return Server.class;
    }

    @Override
    public boolean isPrototype() {
        return false;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
