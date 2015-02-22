package gov.hhs.onc.phiz.logging.logstash.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.composite.loggingevent.MdcJsonProvider;
import org.apache.commons.collections4.CollectionUtils;

public class PhizMdcJsonProvider extends MdcJsonProvider {
    private List<String> includeMdcKeyNames;
    private List<String> excludeMdcKeyNames;

    @Override
    public void writeTo(JsonGenerator jsonGen, ILoggingEvent event) throws IOException {
        Map<String, String> mdcProps = new HashMap<>(event.getMDCPropertyMap());

        if (mdcProps.isEmpty()) {
            return;
        }

        if (!CollectionUtils.isEmpty(this.includeMdcKeyNames)) {
            mdcProps.keySet().retainAll(this.includeMdcKeyNames);
        } else if (!CollectionUtils.isEmpty(this.excludeMdcKeyNames)) {
            mdcProps.keySet().removeAll(this.excludeMdcKeyNames);
        }

        JsonWritingUtils.writeMapEntries(jsonGen,
            mdcProps.entrySet().stream().collect(Collectors.toMap(mdcPropEntry -> PhizLogstashMarkers.buildFieldName(mdcPropEntry.getKey()), Entry::getValue)));
    }

    @Override
    public void setIncludeMdcKeyNames(List<String> includeMdcKeyNames) {
        super.setIncludeMdcKeyNames((this.includeMdcKeyNames = includeMdcKeyNames));
    }

    @Override
    public void setExcludeMdcKeyNames(List<String> excludeMdcKeyNames) {
        super.setExcludeMdcKeyNames((this.excludeMdcKeyNames = excludeMdcKeyNames));
    }
}
