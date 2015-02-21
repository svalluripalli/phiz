package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.crypto.impl.AbstractPhizCryptoFactoryBean;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

public abstract class AbstractPhizSslContextAwareFactoryBean<T> extends AbstractPhizCryptoFactoryBean<T> {
    protected SSLContext sslContext;
    protected SSLParameters sslParams;

    protected AbstractPhizSslContextAwareFactoryBean(Class<T> objClass) {
        super(objClass);
    }

    public SSLContext getSslContext() {
        return this.sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public SSLParameters getSslParameters() {
        return this.sslParams;
    }

    public void setSslParameters(SSLParameters sslParams) {
        this.sslParams = sslParams;
    }
}
