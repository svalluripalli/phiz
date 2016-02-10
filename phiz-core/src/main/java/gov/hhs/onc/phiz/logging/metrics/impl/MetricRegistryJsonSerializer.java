package gov.hhs.onc.phiz.logging.metrics.impl;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import gov.hhs.onc.phiz.logging.logstash.impl.AbstractPhizJsonSerializer;
import gov.hhs.onc.phiz.logging.logstash.impl.PhizLogstashMarkers;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component("jsonSerializerMetricRegistry")
public class MetricRegistryJsonSerializer extends AbstractPhizJsonSerializer<MetricRegistry> {
    private final static String METRIC_KEY_DELIM = ".";

    private final static long serialVersionUID = 0L;

    public MetricRegistryJsonSerializer() {
        super(MetricRegistry.class);
    }

    @Override
    protected void serializeFields(MetricRegistry metricRegistry, JsonGenerator jsonGen, SerializerProvider serializerProv) throws Exception {
        serializeMetricField(jsonGen, null, metricRegistry.getMetrics());
    }

    private static void serializeMetricField(JsonGenerator jsonGen, @Nullable String metricFieldName, Map<String, ?> metricMap) throws IOException {
        boolean metricFieldNameAvailable = (metricFieldName != null);

        if (metricFieldNameAvailable) {
            jsonGen.writeObjectFieldStart(PhizLogstashMarkers.buildFieldName(metricFieldName));
        }

        if (!metricMap.isEmpty()) {
            Map<String, List<String>> metricKeyMap = metricMap.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.groupingBy((metricKey) -> StringUtils.split(metricKey, METRIC_KEY_DELIM, 2)[0]));

            List<String> metricKeys;
            String metricFieldKey;
            Object metricFieldValue;

            for (String metricKeyPrefix : metricKeyMap.keySet()) {
                if (((metricKeys = metricKeyMap.get(metricKeyPrefix)).size() == 1)
                    && !StringUtils.contains((metricFieldKey = metricKeys.get(0)), METRIC_KEY_DELIM)) {
                    jsonGen.writeObjectField(PhizLogstashMarkers.buildFieldName(metricKeyPrefix),
                        (((metricFieldValue = metricMap.get(metricFieldKey)) instanceof Gauge<?>)
                            ? ((Gauge<?>) metricFieldValue).getValue() : metricFieldValue));
                } else {
                    serializeMetricField(jsonGen, metricKeyPrefix,
                        metricKeys.stream().sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toMap((String metricKey) -> StringUtils.removeStartIgnoreCase(metricKey, (metricKeyPrefix + METRIC_KEY_DELIM)),
                                ((Function<String, Object>) metricMap::get))));
                }
            }
        }

        if (metricFieldNameAvailable) {
            jsonGen.writeEndObject();
        }
    }
}
