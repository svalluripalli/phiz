package gov.hhs.onc.phiz.web.ws.iis.impl;

import gov.hhs.onc.phiz.web.ws.iis.IisService;
import gov.hhs.onc.phiz.ws.iis.ConnectivityTestRequestType;
import gov.hhs.onc.phiz.ws.iis.ConnectivityTestResponseType;
import gov.hhs.onc.phiz.ws.iis.UnsupportedOperationFault;
import gov.hhs.onc.phiz.ws.iis.impl.ObjectFactory;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public abstract class AbstractIisService implements IisService {
    @Autowired
    protected ObjectFactory objFactory;

    @Resource
    protected WebServiceContext wsContext;

    protected AbstractApplicationContext appContext;

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
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.appContext = ((AbstractApplicationContext) appContext);
    }
}
