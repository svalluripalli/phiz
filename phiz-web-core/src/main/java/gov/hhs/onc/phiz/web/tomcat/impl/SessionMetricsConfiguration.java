package gov.hhs.onc.phiz.web.tomcat.impl;

import com.codahale.metrics.annotation.Gauge;
import java.util.stream.Stream;
import org.apache.catalina.Context;
import org.apache.catalina.session.ManagerBase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.context.annotation.Configuration;

@ConditionalOnBean({ TomcatEmbeddedServletContainer.class })
@Configuration("sessionMetricsConfiguration")
public class SessionMetricsConfiguration implements InitializingBean {
    @Autowired
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    private TomcatEmbeddedServletContainer embeddedServletContainer;

    private ManagerBase sessionManager;

    @Gauge(name = "http.sessions.active", absolute = true)
    public int getActiveSessions() {
        return this.sessionManager.getActiveSessions();
    }

    @Gauge(name = "http.sessions.active.max", absolute = true)
    public int getMaxActiveSessions() {
        return this.sessionManager.getMaxActiveSessions();
    }

    @Gauge(name = "http.sessions.expired", absolute = true)
    public long getExpiredSessions() {
        return this.sessionManager.getExpiredSessions();
    }

    @Gauge(name = "http.sessions.rejected", absolute = true)
    public int getRejectedSessions() {
        return this.sessionManager.getRejectedSessions();
    }

    @Gauge(name = "http.sessions.alive.time.mean", absolute = true)
    public int getSessionAverageAliveTime() {
        return this.sessionManager.getSessionAverageAliveTime();
    }

    @Gauge(name = "http.sessions.alive.time.max", absolute = true)
    public int getSessionMaxAliveTime() {
        return this.sessionManager.getSessionMaxAliveTime();
    }

    @Gauge(name = "http.sessions.create.rate", absolute = true)
    public int getSessionCreateRate() {
        return this.sessionManager.getSessionCreateRate();
    }

    @Gauge(name = "http.sessions.expire.rate", absolute = true)
    public int getSessionExpireRate() {
        return this.sessionManager.getSessionExpireRate();
    }

    @Gauge(name = "http.sessions.total", absolute = true)
    public long getTotalSessions() {
        return this.sessionManager.getSessionCounter();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.sessionManager =
            ((ManagerBase) ((Context) Stream.of(this.embeddedServletContainer.getTomcat().getHost().findChildren())
                .filter(container -> (container instanceof Context)).findFirst().get()).getManager());
    }
}
