package gov.hhs.onc.phiz.crypto.ssl.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.aop.framework.ProxyFactory;

public class PhizSslServerSocketFactoryFactoryBean extends AbstractPhizCryptoSocketFactoryFactoryBean<SSLServerSocketFactory, SSLServerSocket> {
    public PhizSslServerSocketFactoryFactoryBean() {
        super(SSLServerSocketFactory.class);
    }

    @Override
    public SSLServerSocketFactory getObject() throws Exception {
        return this.objClass.cast(this.buildSocketFactoryProxyFactory(this.sslContext.getServerSocketFactory()).getProxy());
    }

    @Override
    protected ProxyFactory buildSocketFactoryProxyFactory(SSLServerSocketFactory socketFactory) {
        ProxyFactory socketFactoryProxyFactory = super.buildSocketFactoryProxyFactory(socketFactory);
        socketFactoryProxyFactory.addAdvice(((MethodInterceptor) (methodInvocation) -> {
            Method method = methodInvocation.getMethod();
            Object methodReturnValue = method.invoke((!Modifier.isStatic(method.getModifiers()) ? socketFactory : null), methodInvocation.getArguments());

            if (ClassUtils.isAssignable(method.getReturnType(), SSLServerSocket.class)) {
                ((SSLServerSocket) methodReturnValue).setSSLParameters(this.sslParams);
            }

            return methodReturnValue;
        }));

        return socketFactoryProxyFactory;
    }
}
