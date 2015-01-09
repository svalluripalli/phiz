package gov.hhs.onc.phiz.web.ws.jaxws.impl;

import java.util.List;
import java.util.Map;
import org.apache.cxf.Bus;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.spring.JaxWsProxyFactoryBeanDefinitionParser.JAXWSSpringClientProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.Headers;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.springframework.beans.factory.SmartFactoryBean;

public class PhizJaxWsClientProxyFactoryBean extends JAXWSSpringClientProxyFactoryBean implements SmartFactoryBean<Object> {
    private static class SetRequestProtocolHeadersInterceptor extends AbstractPhaseInterceptor<Message> {
        private Map<String, List<String>> reqProtocolHeaders;

        public SetRequestProtocolHeadersInterceptor(Map<String, List<String>> reqProtocolHeaders) {
            super(Phase.PRE_STREAM);

            this.reqProtocolHeaders = reqProtocolHeaders;
        }

        @Override
        public void handleMessage(Message reqMsg) throws Fault {
            Headers.getSetProtocolHeaders(reqMsg).putAll(this.reqProtocolHeaders);
        }
    }

    private AddressingProperties reqAddrProps;

    public PhizJaxWsClientProxyFactoryBean() {
        super();
    }

    public PhizJaxWsClientProxyFactoryBean(Bus bus, String addr, Map<String, List<String>> reqProtocolHeaders, AddressingProperties reqAddrProps) {
        super();

        this.setBus(bus);
        this.setAddress(addr);

        this.reqAddrProps = reqAddrProps;

        this.getOutInterceptors().add(new SetRequestProtocolHeadersInterceptor(reqProtocolHeaders));
    }

    @Override
    public Object create() {
        Object clientObj = super.create();

        ClientProxy.getClient(clientObj).getRequestContext().put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, this.reqAddrProps);

        return clientObj;
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
