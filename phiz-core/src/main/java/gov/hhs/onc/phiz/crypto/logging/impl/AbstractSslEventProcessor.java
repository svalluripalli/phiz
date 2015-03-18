package gov.hhs.onc.phiz.crypto.logging.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import gov.hhs.onc.phiz.crypto.logging.SslEvent;
import gov.hhs.onc.phiz.crypto.logging.SslEventProcessor;
import gov.hhs.onc.phiz.logging.logstash.PhizLogstashTags;
import gov.hhs.onc.phiz.logging.logstash.impl.PhizLogstashMarkers;
import gov.hhs.onc.phiz.utils.PhizStringUtils;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

public abstract class AbstractSslEventProcessor<T extends SslEvent> implements SslEventProcessor<T> {
    private final static String DEBUG_SYS_PROP_NAME = "javax.net.debug";
    private final static String SSL_DEBUG_SYS_PROP_VALUE = "ssl";

    private final static Logger LOGGER = ((Logger) LoggerFactory.getLogger(AbstractSslEventProcessor.class));

    protected ThreadLocal<T> threadEvent;
    protected Set<String> debugSysPropValues = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    protected AbstractSslEventProcessor(Supplier<T> eventCreator, String ... debugSysPropValues) {
        this.threadEvent = ThreadLocal.withInitial(eventCreator);

        this.debugSysPropValues.add(SSL_DEBUG_SYS_PROP_VALUE);
        Stream.of(debugSysPropValues).forEach(this.debugSysPropValues::add);
    }

    @Override
    public boolean canProcessEvent(StackTraceElement[] frames) {
        String debugSysPropValue = System.getProperty(DEBUG_SYS_PROP_NAME);

        return (!StringUtils.isBlank(debugSysPropValue) && this.debugSysPropValues.containsAll(Arrays.asList(PhizStringUtils.tokenize(debugSysPropValue))));
    }

    protected void dispatchEvent(StackTraceElement[] frames, Level level, String msg, T event) {
        LoggingEvent loggingEvent = new LoggingEvent(Logger.FQCN, LOGGER, level, msg, null, null);
        loggingEvent.setCallerData(frames);
        loggingEvent.setMarker(PhizLogstashMarkers.append(PhizLogstashTags.SSL, event));

        LOGGER.callAppenders(loggingEvent);

        this.threadEvent.remove();
    }
}
