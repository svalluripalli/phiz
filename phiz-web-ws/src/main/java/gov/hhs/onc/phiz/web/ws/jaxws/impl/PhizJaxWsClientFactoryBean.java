package gov.hhs.onc.phiz.web.ws.jaxws.impl;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartFactoryBean;

public class PhizJaxWsClientFactoryBean extends JaxWsProxyFactoryBean implements DisposableBean, SmartFactoryBean<Client> {
    private ClientProxy clientProxy;

    public PhizJaxWsClientFactoryBean() {
        super();
    }

    public PhizJaxWsClientFactoryBean(String addr) {
        super();

        this.setAddress(addr);
    }

    @Override
    public void destroy() throws Exception {
        if (this.clientProxy != null) {
            this.clientProxy.close();

            this.clientProxy = null;
        }
    }

    @Override
    public Client getObject() throws Exception {
        return ((Client) this.create());
    }

    @Override
    public Object create() {
        this.configured = true;

        return super.create();
    }

    @Override
    protected ClientProxy clientClientProxy(Client client) {
        return (this.clientProxy = super.clientClientProxy(client));
    }

    @Override
    public boolean isEagerInit() {
        return false;
    }

    @Override
    public Class<?> getObjectType() {
        return Client.class;
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
