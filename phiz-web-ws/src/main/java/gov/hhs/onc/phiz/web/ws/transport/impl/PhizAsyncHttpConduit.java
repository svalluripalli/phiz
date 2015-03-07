package gov.hhs.onc.phiz.web.ws.transport.impl;

import gov.hhs.onc.phiz.web.ws.PhizWsMessageProperties;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
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

    private final static String LAST_TLS_HASH_FIELD_NAME = "lastTlsHash";
    private final static String SSL_CONTEXT_FIELD_NAME = "sslContext";

    private final static Field LAST_TLS_HASH_FIELD = FieldUtils.getDeclaredField(AsyncHTTPConduit.class, LAST_TLS_HASH_FIELD_NAME, true);
    private final static Field SSL_CONTEXT_FIELD = FieldUtils.getDeclaredField(AsyncHTTPConduit.class, SSL_CONTEXT_FIELD_NAME, true);

    private SSLContext sslContext;

    public PhizAsyncHttpConduit(Bus bus, PhizAsyncHttpConduitFactory conduitFactory) throws IOException {
        this(bus, new EndpointInfo(), null, conduitFactory);
    }

    public PhizAsyncHttpConduit(Bus bus, EndpointInfo endpointInfo, @Nullable EndpointReferenceType endpointRef, PhizAsyncHttpConduitFactory conduitFactory)
        throws IOException {
        super(bus, endpointInfo, endpointRef, conduitFactory);
    }

    @Override
    public void initializeSSLEngine(SSLContext sslContext, SSLEngine sslEngine) {
    }

    @Override
    public synchronized SSLContext getSSLContext(TLSClientParameters tlsClientParams) throws GeneralSecurityException {
        try {
            if (SSL_CONTEXT_FIELD.get(this) == null) {
                LAST_TLS_HASH_FIELD.set(this, tlsClientParams.hashCode());
                SSL_CONTEXT_FIELD.set(this, this.sslContext);
            }
        } catch (IllegalAccessException e) {
            throw new GeneralSecurityException("Unable to access asynchronous HTTP(S) conduit field.", e);
        }

        return this.sslContext;
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

    public SSLContext getSslContext() {
        return this.sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }
}
