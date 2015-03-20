package gov.hhs.onc.phiz.test.ws.impl;

import gov.hhs.onc.phiz.test.beans.impl.AbstractPhizHttpServer;
import gov.hhs.onc.phiz.test.ws.PhizTimeoutServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.ssl.SslHandler;
import javax.net.ssl.SSLEngine;

public class PhizTimeoutServerImpl extends AbstractPhizHttpServer implements PhizTimeoutServer {
    private class PhizTimeoutServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private final static long SLEEP_INTERVAL = 1000;

        @Override
        protected void channelRead0(ChannelHandlerContext context, FullHttpRequest reqMsg) throws Exception {
            while (PhizTimeoutServerImpl.this.channel.isOpen()) {
                try {
                    Thread.sleep(SLEEP_INTERVAL);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }
    }

    private SSLEngine sslEngine;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.sslEngine.setUseClientMode(false);
    }

    @Override
    protected void initializePipeline(ChannelPipeline channelPipeline) {
        channelPipeline.addLast(new SslHandler(this.sslEngine));

        super.initializePipeline(channelPipeline);

        channelPipeline.addLast(new PhizTimeoutServerHandler());
    }

    @Override
    public SSLEngine getSslEngine() {
        return this.sslEngine;
    }

    @Override
    public void setSslEngine(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;
    }
}
