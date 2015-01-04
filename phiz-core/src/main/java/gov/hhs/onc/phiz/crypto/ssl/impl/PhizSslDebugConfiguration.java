package gov.hhs.onc.phiz.crypto.ssl.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("sslDebugConfig")
public class PhizSslDebugConfiguration implements DisposableBean {
    private final static String SUN_SEC_PKG_NAME_PREFIX = StringUtils.join(ArrayUtils.toArray("sun", "security", "ssl", StringUtils.EMPTY),
        ClassUtils.PACKAGE_SEPARATOR);

    private final static String SUN_SEC_DEBUG_CLASS_NAME = SUN_SEC_PKG_NAME_PREFIX + "Debug";
    private final static String PRINT_STREAM_CLASS_NAME = PrintStream.class.getName();

    private final static String PROXY_PRINT_STREAM_CLASS_NAME_PREFIX = "$" + PRINT_STREAM_CLASS_NAME + "$$EnhancerBySpringCGLIB$$";

    private final static String PRINT_METHOD_NAME = "print";
    private final static String PRINTLN_METHOD_NAME = "println";

    private final static int THREAD_PRINT_STR_BUILDER_INITIAL_CAPACITY = 256;
    private final static ThreadLocal<StringBuilder> THREAD_PRINT_STR_BUILDER = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(THREAD_PRINT_STR_BUILDER_INITIAL_CAPACITY);
        }
    };

    private final static Map<PrintStream, Consumer<PrintStream>> DELEGATE_PRINT_STREAM_MAP = new HashMap<>(2);

    private final static Logger LOGGER = ((Logger) LoggerFactory.getLogger(PhizSslDebugConfiguration.class));

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
        DELEGATE_PRINT_STREAM_MAP.forEach((delegatePrintStream, delegateStreamSetFunc) -> {
            delegateStreamSetFunc.accept(delegatePrintStream);
        });
    }

    private synchronized static PrintStream buildProxyPrintStream(PrintStream delegatePrintStream, Consumer<PrintStream> delegateStreamSetFunc) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setProxyTargetClass(true);

        proxyFactory.setTargetSource(new SingletonTargetSource(delegatePrintStream) {
            private final static long serialVersionUID = 0L;

            @Override
            public Class<?> getTargetClass() {
                return PrintStream.class;
            }
        });

        proxyFactory.addAdvice(((MethodInterceptor) (invocation) -> {
            Method invocationMethod = invocation.getMethod();
            String invocationMethodName = invocationMethod.getName();
            Object[] invocationArgs = invocation.getArguments();

            if (invocationMethod.getDeclaringClass().getName().equals(PRINT_STREAM_CLASS_NAME)
                && (invocationMethodName.equals(PRINT_METHOD_NAME) || invocationMethodName.equals(PRINTLN_METHOD_NAME)) && (invocationArgs.length == 1)) {
                StackTraceElement[] stackTraceElems = new Throwable().getStackTrace();
                int numStackTraceElems = stackTraceElems.length;
                StackTraceElement stackTraceElem;

                for (int a = 0; a < numStackTraceElems; a++) {
                    if (StringUtils.startsWith((stackTraceElem = stackTraceElems[a]).getClassName(), PROXY_PRINT_STREAM_CLASS_NAME_PREFIX)
                        && stackTraceElem.getMethodName().equals(invocationMethodName)) {
                        if (StringUtils.startsWith(stackTraceElems[++a].getClassName(), SUN_SEC_PKG_NAME_PREFIX)) {
                            while (stackTraceElems[a].getClassName().equals(SUN_SEC_DEBUG_CLASS_NAME)) {
                                a++;
                            }

                            StringBuilder printStrBuilder = THREAD_PRINT_STR_BUILDER.get();
                            printStrBuilder.append(invocationArgs[0]);

                            if (invocationMethodName.equals(PRINTLN_METHOD_NAME)) {
                                LoggingEvent srcEvent = new LoggingEvent(Logger.FQCN, LOGGER, Level.TRACE, printStrBuilder.toString(), null, null);
                                srcEvent.setCallerData(ArrayUtils.subarray(stackTraceElems, a, numStackTraceElems));

                                LOGGER.callAppenders(srcEvent);

                                THREAD_PRINT_STR_BUILDER.remove();
                            }

                            return null;
                        } else {
                            break;
                        }
                    }
                }
            }

            return invocationMethod.invoke(delegatePrintStream, invocationArgs);
        }));

        PrintStream proxyPrintStream = ((PrintStream) proxyFactory.getProxy());

        DELEGATE_PRINT_STREAM_MAP.put(delegatePrintStream, delegateStreamSetFunc);

        delegateStreamSetFunc.accept(proxyPrintStream);

        return proxyPrintStream;
    }
}
