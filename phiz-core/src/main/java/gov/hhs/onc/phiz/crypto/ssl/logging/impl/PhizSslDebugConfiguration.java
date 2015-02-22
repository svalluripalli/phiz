package gov.hhs.onc.phiz.crypto.ssl.logging.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils;
import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils.PhizMethodAdvisor;
import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils.PhizMethodInterceptor;
import gov.hhs.onc.phiz.logging.logstash.PhizLogstashTags;
import gov.hhs.onc.phiz.logging.logstash.impl.PhizLogstashMarkers;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("sslDebugConfiguration")
public class PhizSslDebugConfiguration implements DisposableBean {
    private final static String SUN_SEC_PKG_NAME_PREFIX = StringUtils.join(ArrayUtils.toArray("sun", "security", "ssl", StringUtils.EMPTY),
        ClassUtils.PACKAGE_SEPARATOR);

    private final static String HANDSHAKE_MSG_INNER_CLASS_NAME_PREFIX = SUN_SEC_PKG_NAME_PREFIX + "HandshakeMessage$";
    private final static String ENHANCED_PRINT_STREAM_CLASS_NAME_PREFIX = PhizProxyUtils.ENHANCER_CLASS_NAME_PREFIX + PrintStream.class.getName()
        + PhizProxyUtils.ENHANCER_CLASS_NAME_SUFFIX;

    private final static String DEBUG_CLASS_NAME = SUN_SEC_PKG_NAME_PREFIX + "Debug";
    private final static String HANDSHAKE_MSG_CLIENT_HELLO_INNER_CLASS_NAME = HANDSHAKE_MSG_INNER_CLASS_NAME_PREFIX + "ClientHello";
    private final static String HANDSHAKE_MSG_SERVER_HELLO_INNER_CLASS_NAME = HANDSHAKE_MSG_INNER_CLASS_NAME_PREFIX + "ServerHello";

    private final static Set<String> HANDSHAKE_CLASS_NAMES = Stream
        .of(HANDSHAKE_MSG_CLIENT_HELLO_INNER_CLASS_NAME, HANDSHAKE_MSG_SERVER_HELLO_INNER_CLASS_NAME).collect(Collectors.toSet());

    private final static String PRINT_METHOD_NAME = "print";
    private final static String PRINTLN_METHOD_NAME = "println";

    private final static int THREAD_PRINT_STR_BUILDER_INITIAL_CAPACITY = 64;
    private final static ThreadLocal<StringBuilder> THREAD_PRINT_STR_BUILDER = ThreadLocal.withInitial(() -> new StringBuilder(
        THREAD_PRINT_STR_BUILDER_INITIAL_CAPACITY));

    private final static Map<PrintStream, Consumer<PrintStream>> DELEGATE_PRINT_STREAM_MAP = new HashMap<>(2);

    private final static Logger LOGGER = ((Logger) LoggerFactory.getLogger(PhizSslDebugConfiguration.class));

    private final static PhizMethodAdvisor PRINT_METHODS_ADVISOR = new PhizMethodAdvisor(
        ((PhizMethodInterceptor) (invocation, method, methodName, args, target) -> {
            StackTraceElement[] stackTraceElems = new Throwable().getStackTrace();
            int numStackTraceElems = stackTraceElems.length;
            StackTraceElement stackTraceElem;

            for (int a = 0; a < numStackTraceElems; a++) {
                if (StringUtils.startsWith((stackTraceElem = stackTraceElems[a]).getClassName(), ENHANCED_PRINT_STREAM_CLASS_NAME_PREFIX)
                    && stackTraceElem.getMethodName().equals(methodName)) {
                    if (!StringUtils.startsWith(stackTraceElems[++a].getClassName(), SUN_SEC_PKG_NAME_PREFIX)) {
                        break;
                    }

                    if (args.length != 1) {
                        return null;
                    }

                    while (stackTraceElems[a].getClassName().equals(DEBUG_CLASS_NAME)) {
                        a++;
                    }

                    if (!HANDSHAKE_CLASS_NAMES.contains(stackTraceElems[a].getClassName())) {
                        return null;
                    }

                    StringBuilder printStrBuilder = THREAD_PRINT_STR_BUILDER.get();
                    printStrBuilder.append(args[0]);

                    if (methodName.equals(PRINTLN_METHOD_NAME)) {
                        String msg = StringUtils.trim(StringUtils.strip(printStrBuilder.toString()));

                        // noinspection ConstantConditions
                        if (!msg.isEmpty()) {
                            LoggingEvent srcEvent = new LoggingEvent(Logger.FQCN, LOGGER, Level.TRACE, msg, null, null);
                            srcEvent.setCallerData(ArrayUtils.subarray(stackTraceElems, a, numStackTraceElems));
                            srcEvent.setMarker(PhizLogstashMarkers.append(PhizLogstashTags.SSL));

                            LOGGER.callAppenders(srcEvent);
                        }

                        THREAD_PRINT_STR_BUILDER.remove();
                    }

                    return null;
                }
            }

            return invocation.proceed();
        }), PRINT_METHOD_NAME, PRINTLN_METHOD_NAME);

    @Bean(name = "sslDebugPrintStreamErr")
    public PrintStream getErrPrintStream() {
        return buildProxyPrintStream(System.err, System::setErr);
    }

    @Bean(name = "sslDebugPrintStreamOut")
    public PrintStream getOutPrintStream() {
        return buildProxyPrintStream(System.out, System::setOut);
    }

    @Override
    public synchronized void destroy() throws Exception {
        DELEGATE_PRINT_STREAM_MAP.forEach((delegatePrintStream, delegateStreamSetter) -> delegateStreamSetter.accept(delegatePrintStream));
    }

    private synchronized static PrintStream buildProxyPrintStream(PrintStream delegatePrintStream, Consumer<PrintStream> delegateStreamSetter) {
        PrintStream proxyPrintStream = PhizProxyUtils.buildProxyFactory(delegatePrintStream, PrintStream.class, PRINT_METHODS_ADVISOR).getProxy();

        DELEGATE_PRINT_STREAM_MAP.put(delegatePrintStream, delegateStreamSetter);

        delegateStreamSetter.accept(proxyPrintStream);

        return proxyPrintStream;
    }
}
