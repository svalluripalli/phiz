package gov.hhs.onc.phiz.web.ws.transport.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.cxf.Bus;
import org.apache.cxf.extension.BusExtension;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitFactory;
import org.apache.cxf.transport.http.asyncclient.AsyncHTTPConduitFactory;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.springframework.beans.factory.InitializingBean;

public class PhizAsyncHttpConduitFactory extends AsyncHTTPConduitFactory implements BusExtension, InitializingBean {
    private Map<String, Object> props = new HashMap<>();

    public PhizAsyncHttpConduitFactory(Bus bus) {
        super(bus);
    }

    @Nullable
    @Override
    public HTTPConduit createConduit(Bus bus, EndpointInfo endpointInfo, @Nullable EndpointReferenceType endpointRef) throws IOException {
        return (!this.isShutdown() ? new PhizAsyncHttpConduit(bus, endpointInfo, endpointRef, this) : null);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.update(this.props);
    }

    public Map<String, Object> getProperties() {
        return this.props;
    }

    public void setProperties(Map<String, Object> props) {
        this.props.putAll(props);
    }

    @Override
    public Class<?> getRegistrationType() {
        return HTTPConduitFactory.class;
    }
}
