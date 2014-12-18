package gov.hhs.onc.phiz.web.ws;

import gov.hhs.onc.phiz.ws.ConnectivityTestRequestType;
import gov.hhs.onc.phiz.ws.ConnectivityTestResponseType;
import gov.hhs.onc.phiz.ws.UnsupportedOperationFault;

public interface IisService {
    public ConnectivityTestResponseType connectivityTest(ConnectivityTestRequestType reqParams) throws UnsupportedOperationFault;
}
