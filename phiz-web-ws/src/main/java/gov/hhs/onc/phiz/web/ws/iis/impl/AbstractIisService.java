package gov.hhs.onc.phiz.web.ws.iis.impl;

import gov.hhs.onc.phiz.web.ws.iis.IisService;
import gov.hhs.onc.phiz.ws.iis.ConnectivityTestRequestType;
import gov.hhs.onc.phiz.ws.iis.ConnectivityTestResponseType;
import gov.hhs.onc.phiz.ws.iis.UnsupportedOperationFault;
import gov.hhs.onc.phiz.ws.iis.impl.ObjectFactory;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import org.apache.cxf.Bus;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractIisService implements IisService {
    @Autowired
    protected ObjectFactory objFactory;

    @Resource
    protected WebServiceContext wsContext;

    protected BeanFactory beanFactory;
    protected Bus bus;

    @Override
    public ConnectivityTestResponseType connectivityTest(ConnectivityTestRequestType reqParams) throws UnsupportedOperationFault {
        return this.connectivityTestInternal(reqParams);
    }

    protected ConnectivityTestResponseType connectivityTestInternal(ConnectivityTestRequestType reqParams) {
        ConnectivityTestResponseType respParams = this.objFactory.createConnectivityTestResponseType();
        respParams.setEchoBack(reqParams.getEchoBack());

        return respParams;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public Bus getBus() {
        return this.bus;
    }

    @Override
    public void setBus(Bus bus) {
        this.bus = bus;
    }
}
