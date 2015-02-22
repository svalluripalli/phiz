package gov.hhs.onc.phiz.web.ws.transport.impl;

import gov.hhs.onc.phiz.web.ws.PhizWsMessageProperties;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import javax.annotation.Nullable;
import org.apache.cxf.Bus;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.Address;
import org.apache.cxf.transport.http.asyncclient.AsyncHTTPConduit;
import org.apache.cxf.transport.http.asyncclient.CXFHttpRequest;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class PhizAsyncHttpConduit extends AsyncHTTPConduit {
    private class PhizAsyncWrappedOutputStream extends AsyncWrappedOutputStream {
        private CXFHttpRequest httpReq;

        public PhizAsyncWrappedOutputStream(Message outMsg, boolean cacheReq, boolean chunking, int chunkThreshold, CXFHttpRequest httpReq) {
            super(outMsg, cacheReq, chunking, chunkThreshold, PhizAsyncHttpConduit.this.getConduitName(), httpReq.getURI());

            (this.httpReq = httpReq).setOutputStream(this);
        }

        @Override
        public void close() throws IOException {
            super.close();

            this.outMessage.put(PhizWsMessageProperties.CONTENT_LEN, this.httpReq.getEntity().getContentLength());
        }

        @Override
        protected void updateResponseHeaders(Message inMsg) throws IOException {
            inMsg.put(PhizWsMessageProperties.RESP_CODE_MSG, this.getResponseMessage());

            super.updateResponseHeaders(inMsg);
        }
    }

    public PhizAsyncHttpConduit(Bus bus, PhizAsyncHttpConduitFactory conduitFactory) throws IOException {
        this(bus, new EndpointInfo(), null, conduitFactory);
    }

    public PhizAsyncHttpConduit(Bus bus, EndpointInfo endpointInfo, @Nullable EndpointReferenceType endpointRef, PhizAsyncHttpConduitFactory conduitFactory)
        throws IOException {
        super(bus, endpointInfo, endpointRef, conduitFactory);
    }

    @Override
    protected void setupConnection(Message outMsg, Address addr, HTTPClientPolicy clientPolicy) throws IOException {
        super.setupConnection(outMsg, addr, clientPolicy);

        if (Objects.equals(PhizWsUtils.getProperty(outMsg, USE_ASYNC, Boolean.class), Boolean.TRUE)) {
            outMsg.put(PhizWsMessageProperties.PROTOCOL, outMsg.get(CXFHttpRequest.class).getProtocolVersion().toString());
        }
    }

    @Override
    protected OutputStream createOutputStream(Message outMsg, boolean cacheReq, boolean chunking, int chunkThreshold) throws IOException {
        return (Objects.equals(PhizWsUtils.getProperty(outMsg, USE_ASYNC, Boolean.class), Boolean.TRUE) ? new PhizAsyncWrappedOutputStream(outMsg, cacheReq,
            chunking, chunkThreshold, outMsg.get(CXFHttpRequest.class)) : super.createOutputStream(outMsg, cacheReq, chunking, chunkThreshold));
    }
}
