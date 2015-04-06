package gov.hhs.onc.phiz.crypto.logging.impl;

import ch.qos.logback.classic.Level;
import gov.hhs.onc.phiz.crypto.logging.SslHelloEvent;
import gov.hhs.onc.phiz.crypto.logging.SslHelloEventProcessor;
import gov.hhs.onc.phiz.crypto.ssl.PhizSslLocation;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("sslEventProcHelloImpl")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SslHelloEventProcessorImpl extends AbstractSslEventProcessor<SslHelloEvent> implements SslHelloEventProcessor {
    private final static String HANDSHAKE_MSG_CLASS_NAME_PREFIX = SslDebugPrintStream.SUN_SEC_SSL_PKG_NAME_PREFIX + "HandshakeMessage$";

    private final static String HELLO_CLASS_NAME_SUFFIX = "Hello";

    private final static Map<String, PhizSslLocation> LOC_MAP = Stream.of(PhizSslLocation.values()).collect(
        Collectors.toMap(loc -> (HANDSHAKE_MSG_CLASS_NAME_PREFIX + StringUtils.capitalize(loc.getId()) + HELLO_CLASS_NAME_SUFFIX),
            Function.<PhizSslLocation> identity()));

    private final static String CIPHER_SUITE_DELIM = ", ";

    private final static String CIPHER_SUITES_PATTERN = "([\\w" + CIPHER_SUITE_DELIM + "]+)";

    private final static String PROTOCOL_LINE_PATTERN_PREFIX = "^";
    private final static String CIPHER_SUITES_LINE_PATTERN_PREFIX = "^Cipher Suite";

    private final static String PROTOCOL_LINE_PATTERN_SUFFIX = HELLO_CLASS_NAME_SUFFIX + ", ([^$]+)$";
    private final static String CLIENT_CIPHER_SUITES_LINE_PATTERN_SUFFIX = "s: \\[" + CIPHER_SUITES_PATTERN + "\\]$";
    private final static String SERVER_CIPHER_SUITES_LINE_PATTERN_SUFFIX = ": " + CIPHER_SUITES_PATTERN + "$";

    private final static Map<PhizSslLocation, Pattern> PROTOCOL_LINE_PATTERN_MAP = Stream.of(PhizSslLocation.values()).collect(
        Collectors.toMap(Function.<PhizSslLocation> identity(),
            loc -> Pattern.compile((PROTOCOL_LINE_PATTERN_PREFIX + StringUtils.capitalize(loc.getId()) + PROTOCOL_LINE_PATTERN_SUFFIX))));

    private final static Map<PhizSslLocation, Pattern> CIPHER_SUITES_LINE_PATTERN_MAP = Stream.of(PhizSslLocation.values()).collect(
        Collectors.toMap(Function.<PhizSslLocation> identity(), loc -> Pattern.compile((CIPHER_SUITES_LINE_PATTERN_PREFIX + ((loc == PhizSslLocation.CLIENT)
            ? CLIENT_CIPHER_SUITES_LINE_PATTERN_SUFFIX : SERVER_CIPHER_SUITES_LINE_PATTERN_SUFFIX)))));

    public SslHelloEventProcessorImpl() {
        super(SslHelloEventImpl::new, "handshake");
    }

    @Override
    public synchronized void processEvent(StackTraceElement[] frames, String msg) {
        String callerClassName = frames[0].getClassName();
        PhizSslLocation loc = LOC_MAP.keySet().stream().filter(callerClassName::startsWith).findFirst().map(LOC_MAP::get).orElse(null);
        SslHelloEvent event = this.threadEvent.get();

        if (event.getLocation() == null) {
            Matcher protocolLineMatcher = PROTOCOL_LINE_PATTERN_MAP.get(loc).matcher(msg);

            if (!protocolLineMatcher.matches()) {
                this.threadEvent.remove();

                return;
            }

            event.setLocation(loc);
            event.setProtocol(protocolLineMatcher.group(1));
        } else {
            Matcher cipherSuitesMatcher = CIPHER_SUITES_LINE_PATTERN_MAP.get(loc).matcher(msg);

            if (!cipherSuitesMatcher.matches()) {
                return;
            }

            String[] cipherSuites = StringUtils.splitByWholeSeparator(cipherSuitesMatcher.group(1), CIPHER_SUITE_DELIM);
            event.setCipherSuites(cipherSuites);

            this.dispatchEvent(frames, Level.DEBUG,
                String.format("SSL %s HELLO (protocol=%s, cipherSuites=[%s]).", loc.getId(), event.getProtocol(), StringUtils.join(cipherSuites, ", ")), event);
        }
    }

    @Override
    public boolean canProcessEvent(StackTraceElement[] frames) {
        if (!super.canProcessEvent(frames)) {
            return false;
        }

        String callerClassName = frames[0].getClassName();

        return LOC_MAP.keySet().stream().anyMatch(callerClassName::startsWith);
    }
}
