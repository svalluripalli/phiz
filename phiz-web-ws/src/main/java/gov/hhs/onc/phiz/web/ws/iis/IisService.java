package gov.hhs.onc.phiz.web.ws.iis;

import gov.hhs.onc.phiz.ws.iis.ConnectivityTestRequestType;
import gov.hhs.onc.phiz.ws.iis.ConnectivityTestResponseType;
import gov.hhs.onc.phiz.ws.iis.UnsupportedOperationFault;
import gov.hhs.onc.phiz.ws.iis.impl.ObjectFactory;

public interface IisService {
    public ConnectivityTestResponseType connectivityTest(ConnectivityTestRequestType reqParams) throws UnsupportedOperationFault;

    public ObjectFactory getObjectFactory();

    public void setObjectFactory(ObjectFactory objFactory);
}
