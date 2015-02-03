package gov.hhs.onc.phiz.web.ws.transport.impl;

import java.io.IOException;
import javax.annotation.Nullable;
import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.asyncclient.AsyncHTTPConduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class PhizAsyncHttpConduit extends AsyncHTTPConduit {
    public PhizAsyncHttpConduit(Bus bus, PhizAsyncHttpConduitFactory conduitFactory) throws IOException {
        this(bus, new EndpointInfo(), null, conduitFactory);
    }

    public PhizAsyncHttpConduit(Bus bus, EndpointInfo endpointInfo, @Nullable EndpointReferenceType endpointRef, PhizAsyncHttpConduitFactory conduitFactory)
        throws IOException {
        super(bus, endpointInfo, endpointRef, conduitFactory);
    }
}
