package gov.hhs.onc.phiz.logging.logback.impl;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class RequestMdcConverter extends ClassicConverter {
    private final static String SECTION_PREFIX = " [";
    private final static String SECTION_SUFFIX = "]";

    @Override
    public String convert(ILoggingEvent event) {
        Map<String, String> mdcProps = event.getMDCPropertyMap();

        return (mdcProps.containsKey(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY) ? (SECTION_PREFIX
            + mdcProps.get(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY) + SECTION_SUFFIX) : StringUtils.EMPTY);
    }
}
