package gov.hhs.onc.phiz.crypto.ssl.impl;

import javax.net.ssl.SSLContext;

public abstract class AbstractPhizSslContextAwareFactoryBean<T> extends AbstractPhizSslParametersAwareFactoryBean<T> {
    protected SSLContext sslContext;

    protected AbstractPhizSslContextAwareFactoryBean(Class<T> objClass) {
        super(objClass);
    }

    public SSLContext getSslContext() {
        return this.sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }
}
