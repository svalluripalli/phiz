package gov.hhs.onc.phiz.test.beans.impl;

import gov.hhs.onc.phiz.test.beans.PhizHttpServer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import javax.annotation.Nonnegative;

public abstract class AbstractPhizHttpServer extends AbstractPhizChannelServer implements PhizHttpServer {
    protected int maxContentLen;

    @Override
    protected void initializePipeline(ChannelPipeline channelPipeline) {
        channelPipeline.addLast(new HttpRequestDecoder());
        channelPipeline.addLast(new HttpResponseEncoder());
        channelPipeline.addLast(new HttpObjectAggregator(this.maxContentLen));
    }

    @Nonnegative
    @Override
    public int getMaxContentLength() {
        return this.maxContentLen;
    }

    @Override
    public void setMaxContentLength(@Nonnegative int maxContentLen) {
        this.maxContentLen = maxContentLen;
    }
}
