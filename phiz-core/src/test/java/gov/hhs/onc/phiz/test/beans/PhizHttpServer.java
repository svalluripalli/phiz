package gov.hhs.onc.phiz.test.beans;

import javax.annotation.Nonnegative;

public interface PhizHttpServer extends PhizChannelServer {
    @Nonnegative
    public int getMaxContentLength();

    public void setMaxContentLength(@Nonnegative int maxContentLen);
}
