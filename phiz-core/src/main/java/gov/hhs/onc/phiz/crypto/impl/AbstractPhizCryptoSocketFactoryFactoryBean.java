package gov.hhs.onc.phiz.crypto.impl;

import java.io.Closeable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import org.springframework.aop.framework.ProxyFactory;

public abstract class AbstractPhizCryptoSocketFactoryFactoryBean<T, U extends Closeable> extends AbstractPhizCryptoFactoryBean<T> {
    protected SSLContext sslContext;
    protected SSLParameters sslParams;

    protected AbstractPhizCryptoSocketFactoryFactoryBean(Class<T> objClass) {
        super(objClass);
    }

    protected ProxyFactory buildSocketFactoryProxyFactory(T socketFactory) {
        ProxyFactory socketFactoryProxyFactory = new ProxyFactory();
        socketFactoryProxyFactory.setProxyTargetClass(true);
        socketFactoryProxyFactory.setTargetClass(this.objClass);

        return socketFactoryProxyFactory;
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
