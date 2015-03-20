package gov.hhs.onc.phiz.web.ws.iis;

import gov.hhs.onc.phiz.ws.iis.ConnectivityTestRequestType;
import gov.hhs.onc.phiz.ws.iis.ConnectivityTestResponseType;
import gov.hhs.onc.phiz.ws.iis.UnsupportedOperationFault;
import org.apache.cxf.Bus;
import org.springframework.beans.factory.BeanFactoryAware;

public interface IisService extends BeanFactoryAware {
    public ConnectivityTestResponseType connectivityTest(ConnectivityTestRequestType reqParams) throws UnsupportedOperationFault;

    public Bus getBus();

    public void setBus(Bus bus);
}
