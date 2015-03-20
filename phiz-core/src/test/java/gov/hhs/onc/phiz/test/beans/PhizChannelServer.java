package gov.hhs.onc.phiz.test.beans;

import io.netty.channel.ChannelOption;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;

public interface PhizChannelServer extends InitializingBean, PhizServer {
    public Map<ChannelOption<?>, Object> getChannelOptions();

    public void setChannelOptions(Map<ChannelOption<?>, Object> channelOpts);
}
