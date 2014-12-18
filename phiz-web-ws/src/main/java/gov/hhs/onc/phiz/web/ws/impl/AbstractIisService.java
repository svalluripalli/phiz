package gov.hhs.onc.phiz.web.ws.impl;

import gov.hhs.onc.phiz.web.ws.IisService;
import gov.hhs.onc.phiz.ws.ConnectivityTestRequestType;
import gov.hhs.onc.phiz.ws.ConnectivityTestResponseType;
import gov.hhs.onc.phiz.ws.UnsupportedOperationFault;
import gov.hhs.onc.phiz.ws.impl.ObjectFactory;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractIisService implements IisService {
    @Autowired
    protected ObjectFactory objFactory;

    @Resource
    protected WebServiceContext wsContext;

    @Override
    public ConnectivityTestResponseType connectivityTest(ConnectivityTestRequestType reqParams) throws UnsupportedOperationFault {
        return this.connectivityTestInternal(reqParams);
    }

    protected ConnectivityTestResponseType connectivityTestInternal(ConnectivityTestRequestType reqParams) {
        ConnectivityTestResponseType respParams = this.objFactory.createConnectivityTestResponseType();
        respParams.setEchoBack(reqParams.getEchoBack());

        return respParams;
    }
}
