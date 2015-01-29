package gov.hhs.onc.phiz.logging.logstash.impl;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.HealthCheckModule;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.logstash.logback.decorate.JsonFactoryDecorator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component("jsonFactoryDecoratorLogstash")
public class PhizLogstashJsonFactoryDecorator implements JsonFactoryDecorator {
    private static class PhizMetricsModule extends Module {
        public final static String MODULE_NAME = "phiz-metrics";
        public final static Version MODULE_VERSION = new Version(1, 0, 0, StringUtils.EMPTY, "gov.hhs.onc.phiz", MODULE_NAME);

        public final static PhizMetricsModule INSTANCE = new PhizMetricsModule();

        @Override
        public void setupModule(SetupContext setupContext) {
            setupContext.addSerializers(new SimpleSerializers(Arrays.<JsonSerializer<?>> asList(PhizMetricRegistrySerializer.INSTANCE)));
        }

        @Override
        public String getModuleName() {
            return MODULE_NAME;
        }

        @Override
        public Version version() {
            return MODULE_VERSION;
        }
    }

    private static class PhizMetricRegistrySerializer extends StdSerializer<MetricRegistry> {
        public final static PhizMetricRegistrySerializer INSTANCE = new PhizMetricRegistrySerializer();

        private final static String METRIC_KEY_DELIM = ".";

        private final static String VERSION_FIELD_NAME = "version";
        private final static String GAUGES_FIELD_NAME = "gauges";
        private final static String COUNTERS_FIELD_NAME = "counters";
        private final static String HISTOGRAMS_FIELD_NAME = "histograms";
        private final static String METERS_FIELD_NAME = "meters";
        private final static String TIMERS_FIELD_NAME = "timers";

        private final static long serialVersionUID = 0L;

        public PhizMetricRegistrySerializer() {
            super(MetricRegistry.class);
        }

        @Override
        public void serialize(MetricRegistry metricRegistry, JsonGenerator jsonGen, SerializerProvider serializerProv) throws IOException {
            jsonGen.writeStartObject();
            jsonGen.writeStringField(VERSION_FIELD_NAME, PhizMetricsModule.MODULE_VERSION.toString());

            serializeMetrics(jsonGen, GAUGES_FIELD_NAME, metricRegistry.getGauges());
            serializeMetrics(jsonGen, COUNTERS_FIELD_NAME, metricRegistry.getCounters());
            serializeMetrics(jsonGen, HISTOGRAMS_FIELD_NAME, metricRegistry.getHistograms());
            serializeMetrics(jsonGen, METERS_FIELD_NAME, metricRegistry.getMeters());
            serializeMetrics(jsonGen, TIMERS_FIELD_NAME, metricRegistry.getTimers());

            jsonGen.writeEndObject();
        }

        private static void serializeMetrics(JsonGenerator jsonGen, String metricFieldName, Map<String, ?> metricMap) throws IOException {
            jsonGen.writeObjectFieldStart(PhizLogstashMarkers.buildFieldName(metricFieldName));

            if (!metricMap.isEmpty()) {
                Map<String, List<String>> metricKeyMap =
                    metricMap.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER)
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
                        serializeMetrics(
                            jsonGen,
                            metricKeyPrefix,
                            metricKeys
                                .stream()
                                .sorted(String.CASE_INSENSITIVE_ORDER)
                                .collect(
                                    Collectors.toMap((String metricKey) -> StringUtils.removeStartIgnoreCase(metricKey, (metricKeyPrefix + METRIC_KEY_DELIM)),
                                        metricMap::get)));
                    }
                }
            }

            jsonGen.writeEndObject();
        }
    }

    @Override
    public MappingJsonFactory decorate(MappingJsonFactory jsonFactory) {
        ObjectMapper objMapper = jsonFactory.getCodec();

        objMapper.disable(MapperFeature.AUTO_DETECT_CREATORS);
        objMapper.disable(MapperFeature.AUTO_DETECT_FIELDS);
        objMapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
        objMapper.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
        objMapper.disable(MapperFeature.AUTO_DETECT_SETTERS);

        objMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        objMapper.registerModules(new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, true), new HealthCheckModule(), PhizMetricsModule.INSTANCE);

        return jsonFactory;
    }
}
