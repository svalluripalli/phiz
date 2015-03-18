package gov.hhs.onc.phiz.crypto.logging.impl;

import gov.hhs.onc.phiz.crypto.logging.SslDebugPrintStreamType;
import gov.hhs.onc.phiz.crypto.logging.SslEventProcessor;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

public class SslDebugPrintStream extends PrintStream implements DisposableBean, InitializingBean {
    private static enum SslDebugInvocationStatus {
        PROPAGATE, REJECT, ACCEPT
    }

    private static class SslDebugInvocation {
        private SslDebugInvocationStatus status;
        private SslEventProcessor<?> eventProc;
        private StackTraceElement[] frames;

        public SslDebugInvocation(SslDebugInvocationStatus status) {
            this(status, null);
        }

        public SslDebugInvocation(SslDebugInvocationStatus status, @Nullable StackTraceElement[] frames) {
            this(status, frames, null);
        }

        public SslDebugInvocation(SslDebugInvocationStatus status, @Nullable StackTraceElement[] frames, @Nullable SslEventProcessor<?> eventProc) {
            this.status = status;
            this.eventProc = eventProc;
            this.frames = frames;
        }

        @Nullable
        public SslEventProcessor<?> getEventProcessor() {
            return this.eventProc;
        }

        @Nullable
        public StackTraceElement[] getFrames() {
            return this.frames;
        }

        public SslDebugInvocationStatus getStatus() {
            return this.status;
        }
    }

    public final static String SUN_PKG_NAME_PREFIX = "sun.";
    public final static String SUN_MISC_PKG_NAME_PREFIX = SUN_PKG_NAME_PREFIX + "misc.";
    public final static String SUN_SEC_PKG_NAME_PREFIX = SUN_PKG_NAME_PREFIX + "security.";
    public final static String SUN_SEC_SSL_PKG_NAME_PREFIX = SUN_SEC_PKG_NAME_PREFIX + "ssl.";
    public final static String SUN_SEC_UTIL_PKG_NAME_PREFIX = SUN_SEC_PKG_NAME_PREFIX + "util.";

    private final static String DEBUG_CLASS_NAME_SUFFIX = "Debug";
    private final static String ENC_CLASS_NAME_SUFFIX = "Encoder";

    private final static String[] SKIP_INVOKER_CLASS_NAMES = Stream.concat(
        ClassUtils.convertClassesToClassNames(Arrays.asList(SslDebugPrintStream.class, OutputStreamWriter.class, PrintStream.class)).stream(),
        Stream.of((SUN_PKG_NAME_PREFIX + "nio.cs.StreamEncoder"), (SUN_SEC_SSL_PKG_NAME_PREFIX + DEBUG_CLASS_NAME_SUFFIX),
            (SUN_SEC_UTIL_PKG_NAME_PREFIX + DEBUG_CLASS_NAME_SUFFIX))).toArray(String[]::new);

    private final static String[] REJECT_INVOKER_CLASS_NAMES = ArrayUtils.toArray((SUN_MISC_PKG_NAME_PREFIX + "Character" + ENC_CLASS_NAME_SUFFIX),
        (SUN_MISC_PKG_NAME_PREFIX + "HexDump" + ENC_CLASS_NAME_SUFFIX));

    private final static String MSG_STRIP_CHARS = "*";

    private final static Map<SslDebugPrintStreamType, ThreadLocal<StringBuilder>> THREAD_BUILDER_MAP = Stream.of(SslDebugPrintStreamType.values()).collect(
        Collectors.toMap(Function.<SslDebugPrintStreamType> identity(), type -> ThreadLocal.withInitial(() -> new StringBuilder(64))));

    @Resource(name = "charsetUtf8")
    private Charset charset;

    private SslDebugPrintStreamType type;
    private Set<SslEventProcessor<?>> eventProcs = new TreeSet<>(AnnotationAwareOrderComparator.INSTANCE);

    public SslDebugPrintStream(SslDebugPrintStreamType type) {
        super(type.getGetter().get());

        this.type = type;
    }

    @Override
    public void println(double data) {
        this.println(Double.toString(data));
    }

    @Override
    public void println(float data) {
        this.println(Float.toString(data));
    }

    @Override
    public void println(long data) {
        this.println(Long.toString(data));
    }

    @Override
    public void println(int data) {
        this.println(Integer.toString(data));
    }

    @Override
    public void println(boolean data) {
        this.println(Boolean.toString(data));
    }

    @Override
    public void println(Object data) {
        this.println(data.toString());
    }

    @Override
    public void println(String data) {
        this.println(data.toCharArray());
    }

    @Override
    public void println(char data) {
        this.println(new char[] { data });
    }

    @Override
    public void println(@SuppressWarnings({ "NullableProblems" }) char[] data) {
        SslDebugInvocation invocation = processInvocation();

        switch (invocation.getStatus()) {
            case ACCEPT:
                processDataEvent(this.type, invocation.getFrames(), invocation.getEventProcessor(), data);
                break;

            case PROPAGATE:
                super.println(data);
                break;
        }
    }

    @Override
    public void println() {
        SslDebugInvocation invocation = processInvocation();

        switch (invocation.getStatus()) {
            case ACCEPT:
                processDataEvent(this.type, invocation.getFrames(), invocation.getEventProcessor());
                break;

            case PROPAGATE:
                super.println();
                break;
        }
    }

    @Override
    public void print(double data) {
        this.print(Double.toString(data));
    }

    @Override
    public void print(float data) {
        this.print(Float.toString(data));
    }

    @Override
    public void print(long data) {
        this.print(Long.toString(data));
    }

    @Override
    public void print(int data) {
        this.print(Integer.toString(data));
    }

    @Override
    public void print(boolean data) {
        this.print(Boolean.toString(data));
    }

    @Override
    public void print(Object data) {
        this.print(data.toString());
    }

    @Override
    public void print(String data) {
        this.print(data.toCharArray());
    }

    @Override
    public void print(char data) {
        this.print(new char[] { data });
    }

    @Override
    public void print(@SuppressWarnings({ "NullableProblems" }) char[] data) {
        switch (processInvocation().getStatus()) {
            case ACCEPT:
                processData(this.type, data);
                break;

            case PROPAGATE:
                super.print(data);
                break;
        }
    }

    @Override
    public void write(int data) {
        this.write(new byte[] { ((byte) data) });
    }

    @Override
    public void write(@SuppressWarnings({ "NullableProblems" }) byte[] data) {
        this.write(data, 0, data.length);
    }

    @Override
    public void write(@SuppressWarnings({ "NullableProblems" }) byte[] data, int dataOffset, int dataLen) {
        switch (processInvocation().getStatus()) {
            case ACCEPT:
                processData(this.type, new String(data, this.charset).toCharArray());
                break;

            case PROPAGATE:
                super.write(data, dataOffset, dataLen);
                break;
        }
    }

    @Override
    public synchronized void destroy() throws Exception {
        this.type.getSetter().accept(((PrintStream) this.out));
    }

    @Override
    public synchronized void afterPropertiesSet() throws Exception {
        this.type.getSetter().accept(this);
    }

    private static void processDataEvent(SslDebugPrintStreamType type, StackTraceElement[] frames, SslEventProcessor<?> eventProc) {
        // noinspection NullArgumentToVariableArgMethod
        processDataEvent(type, frames, eventProc, null);
    }

    private static void processDataEvent(SslDebugPrintStreamType type, StackTraceElement[] frames, SslEventProcessor<?> eventProc, @Nullable char ... data) {
        ThreadLocal<StringBuilder> threadBuilder = THREAD_BUILDER_MAP.get(type);

        // noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (threadBuilder) {
            StringBuilder builder = threadBuilder.get();

            if (data != null) {
                // noinspection ConstantConditions
                builder.append(data);
            }

            String msg = StringUtils.trim(StringUtils.strip(builder.toString(), MSG_STRIP_CHARS));

            // noinspection ConstantConditions
            if (!msg.isEmpty()) {
                eventProc.processEvent(frames, msg);
            }

            threadBuilder.remove();
        }
    }

    private static void processData(SslDebugPrintStreamType type, char ... data) {
        ThreadLocal<StringBuilder> threadBuilder = THREAD_BUILDER_MAP.get(type);

        // noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (threadBuilder) {
            threadBuilder.get().append(data);
        }
    }

    private SslDebugInvocation processInvocation() {
        StackTraceElement[] frames = new Throwable().getStackTrace();

        for (int a = 0; a < frames.length; a++) {
            if (!StringUtils.startsWithAny(frames[a].getClassName(), SKIP_INVOKER_CLASS_NAMES)) {
                frames = ArrayUtils.subarray(frames, a, frames.length);

                break;
            }
        }

        final StackTraceElement[] invokerFrames = frames;
        String invokerClassName = invokerFrames[0].getClassName();
        boolean propagateInvocation;
        SslEventProcessor<?> invokerEventProc = null;

        if (StringUtils.startsWithAny(invokerClassName, REJECT_INVOKER_CLASS_NAMES)
            || (!(propagateInvocation = !invokerClassName.startsWith(SUN_SEC_SSL_PKG_NAME_PREFIX)) && ((invokerEventProc =
                this.eventProcs.stream().filter(eventProc -> eventProc.canProcessEvent(invokerFrames)).findFirst().orElse(null)) == null))) {
            return new SslDebugInvocation(SslDebugInvocationStatus.REJECT);
        }

        return new SslDebugInvocation((propagateInvocation ? SslDebugInvocationStatus.PROPAGATE : SslDebugInvocationStatus.ACCEPT), invokerFrames,
            invokerEventProc);
    }

    public Set<SslEventProcessor<?>> getEventProcessors() {
        return this.eventProcs;
    }

    public void setEventProcessors(Set<SslEventProcessor<?>> eventProcs) {
        this.eventProcs.clear();
        this.eventProcs.addAll(eventProcs);
    }
}
