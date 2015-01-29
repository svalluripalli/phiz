package gov.hhs.onc.phiz.logging.metrics.impl;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import gov.hhs.onc.phiz.beans.factory.impl.AbstractPhizFactoryBean;
import java.util.Map;

public class PhizMetricRegistryFactoryBean extends AbstractPhizFactoryBean<MetricRegistry> {
    private MetricRegistry metricRegistry = new MetricRegistry();

    public PhizMetricRegistryFactoryBean() {
        super(MetricRegistry.class);
    }

    @Override
    public MetricRegistry getObject() throws Exception {
        return this.metricRegistry;
    }

    public void setMetrics(Map<String, Metric> metrics) {
        this.metricRegistry.registerAll(() -> metrics);
    }
}
