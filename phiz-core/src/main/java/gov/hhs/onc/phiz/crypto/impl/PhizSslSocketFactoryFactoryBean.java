package gov.hhs.onc.phiz.crypto.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.aop.framework.ProxyFactory;

public class PhizSslSocketFactoryFactoryBean extends AbstractPhizCryptoSocketFactoryFactoryBean<SSLSocketFactory, SSLSocket> {
    private Set<HandshakeCompletedListener> handshakeCompletedListeners;

    public PhizSslSocketFactoryFactoryBean() {
        super(SSLSocketFactory.class);
    }

    @Override
    public SSLSocketFactory getObject() throws Exception {
        return this.objClass.cast(this.buildSocketFactoryProxyFactory(this.sslContext.getSocketFactory()).getProxy());
    }

    @Override
    protected ProxyFactory buildSocketFactoryProxyFactory(SSLSocketFactory socketFactory) {
        ProxyFactory socketFactoryProxyFactory = super.buildSocketFactoryProxyFactory(socketFactory);
        socketFactoryProxyFactory.addAdvice(((MethodInterceptor) (methodInvocation) -> {
            Method method = methodInvocation.getMethod();
            Object methodReturnValue = method.invoke((!Modifier.isStatic(method.getModifiers()) ? socketFactory : null), methodInvocation.getArguments());

            if (ClassUtils.isAssignable(method.getReturnType(), SSLSocket.class)) {
                SSLSocket sslSocket = ((SSLSocket) methodReturnValue);
                sslSocket.setSSLParameters(this.sslParams);

                Optional.of(this.handshakeCompletedListeners).ifPresent(
                    (handshakeCompletedListeners) -> handshakeCompletedListeners.stream().forEach(sslSocket::addHandshakeCompletedListener));
            }

            return methodReturnValue;
        }));

        return socketFactoryProxyFactory;
    }

    public Set<HandshakeCompletedListener> getHandshakeCompletedListeners() {
        return this.handshakeCompletedListeners;
    }

    public void setHandshakeCompletedListeners(Set<HandshakeCompletedListener> handshakeCompletedListeners) {
        this.handshakeCompletedListeners = handshakeCompletedListeners;
    }
}
