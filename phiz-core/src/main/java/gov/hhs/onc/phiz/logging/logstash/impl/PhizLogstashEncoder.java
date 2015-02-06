package gov.hhs.onc.phiz.logging.logstash.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import java.nio.charset.Charset;
import java.util.List;
import net.logstash.logback.composite.JsonProvider;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;

public class PhizLogstashEncoder extends LoggingEventCompositeJsonEncoder {
    public void setEncodingCharset(Charset enc) {
        this.setEncoding(enc.name());
    }

    public void setProviderItems(List<? extends JsonProvider<ILoggingEvent>> provs) {
        JsonProviders<ILoggingEvent> provsContainer = this.getProviders();

        provs.forEach(provsContainer::addProvider);
    }
}
