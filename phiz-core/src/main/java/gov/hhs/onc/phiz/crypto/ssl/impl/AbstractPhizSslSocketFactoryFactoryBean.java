package gov.hhs.onc.phiz.crypto.ssl.impl;

import java.io.Closeable;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

public abstract class AbstractPhizSslSocketFactoryFactoryBean<T, U extends Closeable> extends AbstractPhizSslContextAwareFactoryBean<T> {
    protected AbstractPhizSslSocketFactoryFactoryBean(Class<T> objClass) {
        super(objClass);
    }

    @Override
    public T getObject() throws Exception {
        return this.objClass.cast(this.buildProxyFactory().getProxy());
    }

    protected abstract AspectJProxyFactory buildProxyFactory();
}
