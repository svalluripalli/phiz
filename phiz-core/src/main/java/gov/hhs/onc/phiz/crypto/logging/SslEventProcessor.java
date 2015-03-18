package gov.hhs.onc.phiz.crypto.logging;

public interface SslEventProcessor<T extends SslEvent> {
    public void processEvent(StackTraceElement[] frames, String msg);

    public boolean canProcessEvent(StackTraceElement[] frames);
}
