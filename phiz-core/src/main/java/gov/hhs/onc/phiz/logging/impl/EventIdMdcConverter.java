package gov.hhs.onc.phiz.logging.impl;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class EventIdMdcConverter extends ClassicConverter {
    public final static String EVENT_ID_MDC_KEY = "eventId";

    private final static String SECTION_PREFIX = " [";
    private final static String SECTION_SUFFIX = "]";

    @Override
    public String convert(ILoggingEvent event) {
        Map<String, String> mdcProps = event.getMDCPropertyMap();

        return (mdcProps.containsKey(EVENT_ID_MDC_KEY) ? (SECTION_PREFIX + mdcProps.get(EVENT_ID_MDC_KEY) + SECTION_SUFFIX) : StringUtils.EMPTY);
    }
}
