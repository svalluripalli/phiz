package gov.hhs.onc.phiz.data.queue;

public interface PhizMessageHandler<T, U> {
    public U handleMessage(T reqMsg);
}
