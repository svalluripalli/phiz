package gov.hhs.onc.phiz.web.ws.iis;

import gov.hhs.onc.phiz.ws.iis.ConnectivityTestRequestType;
import gov.hhs.onc.phiz.ws.iis.ConnectivityTestResponseType;
import gov.hhs.onc.phiz.ws.iis.UnsupportedOperationFault;
import org.springframework.context.ApplicationContextAware;

public interface IisService extends ApplicationContextAware {
    public ConnectivityTestResponseType connectivityTest(ConnectivityTestRequestType reqParams) throws UnsupportedOperationFault;
}
