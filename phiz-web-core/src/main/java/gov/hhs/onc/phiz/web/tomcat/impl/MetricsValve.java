package gov.hhs.onc.phiz.web.tomcat.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.ryantenney.metrics.annotation.Metric;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("valveMetrics")
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class MetricsValve extends ValveBase {
    @Metric(name = "http.requests.active", absolute = true)
    private Counter activeReqsCounter = new Counter();

    @Metric(name = "http.requests.elapsed.time", absolute = true)
    private Timer reqsElapsedTimeTimer = new Timer();

    @Override
    public void invoke(Request req, Response resp) throws IOException, ServletException {
        this.activeReqsCounter.inc();

        try {
            this.getNext().invoke(req, resp);
        } finally {
            this.activeReqsCounter.dec();
            this.reqsElapsedTimeTimer.update((resp.getCoyoteResponse().getCommitTime() - req.getCoyoteRequest().getStartTime()), TimeUnit.MILLISECONDS);
        }
    }
}
