package gov.hhs.onc.phiz.web.tomcat.impl;

import com.codahale.metrics.annotation.Gauge;
import gov.hhs.onc.phiz.context.PhizProfiles;
import java.util.stream.Stream;
import org.apache.catalina.Context;
import org.apache.catalina.session.ManagerBase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration("sessionMetricsConfiguration")
@Profile({ PhizProfiles.CONTEXT_WEB })
public class SessionMetricsConfiguration implements InitializingBean {
    @Autowired
    private PhizTomcatEmbeddedServletContainerFactory embeddedServletContainerFactory;

    private ManagerBase sessionManager;

    @Gauge(name = "http.session.active", absolute = true)
    public int getActiveSessions() {
        return this.sessionManager.getActiveSessions();
    }

    @Gauge(name = "http.session.active.max", absolute = true)
    public int getMaxActiveSessions() {
        return this.sessionManager.getMaxActiveSessions();
    }

    @Gauge(name = "http.session.expired", absolute = true)
    public long getExpiredSessions() {
        return this.sessionManager.getExpiredSessions();
    }

    @Gauge(name = "http.session.rejected", absolute = true)
    public int getRejectedSessions() {
        return this.sessionManager.getRejectedSessions();
    }

    @Gauge(name = "http.session.alive.time.mean", absolute = true)
    public int getSessionAverageAliveTime() {
        return this.sessionManager.getSessionAverageAliveTime();
    }

    @Gauge(name = "http.session.alive.time.max", absolute = true)
    public int getSessionMaxAliveTime() {
        return this.sessionManager.getSessionMaxAliveTime();
    }

    @Gauge(name = "http.session.create.rate", absolute = true)
    public int getSessionCreateRate() {
        return this.sessionManager.getSessionCreateRate();
    }

    @Gauge(name = "http.session.expire.rate", absolute = true)
    public int getSessionExpireRate() {
        return this.sessionManager.getSessionExpireRate();
    }

    @Gauge(name = "http.session.total", absolute = true)
    public long getTotalSessions() {
        return this.sessionManager.getSessionCounter();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.sessionManager =
            ((ManagerBase) ((Context) Stream.of(this.embeddedServletContainerFactory.getTomcat().getHost().findChildren())
                .filter(container -> (container instanceof Context)).findFirst().get()).getManager());
    }
}
