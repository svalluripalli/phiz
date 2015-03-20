package gov.hhs.onc.phiz.test.beans;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;

public interface PhizServer extends InitializingBean, SmartLifecycle {
    public String getHost();

    public void setHost(String host);

    public void setPhase(int phase);

    public int getPort();

    public void setPort(int port);
}
