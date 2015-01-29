package gov.hhs.onc.phiz.logging.metrics.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.github.sebhoss.warnings.CompilerWarnings;
import gov.hhs.onc.phiz.logging.logstash.impl.PhizLogstashMarkers;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

public class LogstashReporter extends ScheduledReporter implements SmartLifecycle {
    private final static String METRICS_MARKER_NAME = "metrics";
    private final static String METRICS_MARKER_FIELD_NAME = "metrics";

    private final static Logger LOGGER = LoggerFactory.getLogger(LogstashReporter.class);

    private MetricRegistry metricRegistry;
    private long period;
    private boolean running;

    public LogstashReporter(MetricRegistry metricRegistry) {
        super(metricRegistry, "reporterLogstash", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.SECONDS);

        this.metricRegistry = metricRegistry;
    }

    @Override
    @SuppressWarnings({ CompilerWarnings.RAWTYPES })
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms,
        SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        LOGGER.info(
            PhizLogstashMarkers.append(METRICS_MARKER_NAME, Markers.append(METRICS_MARKER_FIELD_NAME, this.metricRegistry)),
            String.format("Metrics (numGauges=%d, numCounters=%d, numHistograms=%d, numMeters=%d, numTimers=%d).", gauges.size(), counters.size(),
                histograms.size(), meters.size(), timers.size()));
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();

        callback.run();
    }

    @Override
    public void stop() {
        if (this.isRunning()) {
            super.stop();

            this.running = false;
        }
    }

    @Override
    public void start() {
        if (!this.isRunning()) {
            this.start(this.period, TimeUnit.SECONDS);

            this.running = true;
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    public long getPeriod() {
        return this.period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }
}
