package gov.hhs.onc.phiz.web.ws.iis.impl;

import gov.hhs.onc.phiz.web.ws.iis.IisService;
import gov.hhs.onc.phiz.ws.iis.ConnectivityTestRequestType;
import gov.hhs.onc.phiz.ws.iis.ConnectivityTestResponseType;
import gov.hhs.onc.phiz.ws.iis.UnsupportedOperationFault;
import gov.hhs.onc.phiz.ws.iis.impl.ObjectFactory;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

public abstract class AbstractIisService implements IisService {
    @Resource
    protected WebServiceContext wsContext;

    protected ObjectFactory objFactory;

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
    public ObjectFactory getObjectFactory() {
        return this.objFactory;
    }

    @Override
    public void setObjectFactory(ObjectFactory objFactory) {
        this.objFactory = objFactory;
    }
}
