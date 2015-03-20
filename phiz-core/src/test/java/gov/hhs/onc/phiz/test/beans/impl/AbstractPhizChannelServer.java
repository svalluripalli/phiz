package gov.hhs.onc.phiz.test.beans.impl;

import com.github.sebhoss.warnings.CompilerWarnings;
import gov.hhs.onc.phiz.test.beans.PhizChannelServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.Map;

public abstract class AbstractPhizChannelServer extends AbstractPhizServer implements PhizChannelServer {
    protected Map<ChannelOption<?>, Object> channelOpts;
    protected String host;
    protected int port;
    protected Channel channel;

    @Override
    public boolean isRunning() {
        return ((this.channel != null) && this.channel.isActive());
    }

    @Override
    protected void stopInternal() throws Exception {
        this.channel.close();
    }

    @Override
    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    protected void startInternal() throws Exception {
        EventLoopGroup acceptorEventLoopGroup = new NioEventLoopGroup(1), workerEventLoopGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            this.channelOpts.forEach((channelOpt, channelOptValue) -> serverBootstrap.option(((ChannelOption<Object>) channelOpt), channelOptValue));

            serverBootstrap.group(acceptorEventLoopGroup, workerEventLoopGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        AbstractPhizChannelServer.this.initializePipeline(channel.pipeline());
                    }
                });

            this.channel = serverBootstrap.bind(this.host, this.port).sync().channel();
        } catch (Exception e) {
            acceptorEventLoopGroup.shutdownGracefully();
            workerEventLoopGroup.shutdownGracefully();

            throw e;
        }
    }

    protected abstract void initializePipeline(ChannelPipeline channelPipeline);

    @Override
    public Map<ChannelOption<?>, Object> getChannelOptions() {
        return this.channelOpts;
    }

    @Override
    public void setChannelOptions(Map<ChannelOption<?>, Object> channelOpts) {
        this.channelOpts = channelOpts;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }
}
