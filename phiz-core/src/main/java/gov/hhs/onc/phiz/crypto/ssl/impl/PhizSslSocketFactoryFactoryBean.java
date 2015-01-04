package gov.hhs.onc.phiz.crypto.ssl.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;

public class PhizSslSocketFactoryFactoryBean extends AbstractPhizCryptoSocketFactoryFactoryBean<SSLSocketFactory, SSLSocket> {
    private final static Logger LOGGER = LoggerFactory.getLogger(PhizSslSocketFactoryFactoryBean.class);

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

                // TEMP: dev
            // @formatter:off
                /*
                ProxyFactory socketProxyFactory = new ProxyFactory();
                socketProxyFactory.setProxyTargetClass(true);
                socketProxyFactory.setTarget(methodReturnValue);
                socketProxyFactory.setTargetClass(SSLSocket.class);

                socketProxyFactory.addAdvice(((MethodInterceptor) (socketMethodInvocation) -> {
                    Method socketMethod = socketMethodInvocation.getMethod();

                    if (socketMethod.getName().equals("startHandshake")) {
                        Stream.of(socketMethodInvocation.getThis().getClass().getFields())
                            .filter((socketField) -> ClassUtils.isAssignable(socketField.getType(), SSLSession.class)).findFirst()
                            .ifPresent((socketSessionField) -> {
                                try {
                                    AccessController.doPrivileged((PrivilegedExceptionAction<Void>) () -> {
                                        SSLSession socketSession = ((SSLSession) socketSessionField.get(socketMethodInvocation.getThis()));

                                        if ((socketSession != null) && (socketSession.isValid())) {
                                            socketSession.invalidate();

                                        LOGGER.error(String.format("SSL socket session invalidated: %s", ReflectionToStringBuilder.toString(socketSession)));
                                    }

                                    return null;
                                })  ;
                                } catch (PrivilegedActionException ignored) {
                                }
                            });
                    }

                    return socketMethodInvocation.proceed();
                }));

                methodReturnValue = ((SSLSocket) socketProxyFactory.getProxy());
                */
                // @formatter:on
        }

        return methodReturnValue;
    })) ;

        return socketFactoryProxyFactory;
    }

    public Set<HandshakeCompletedListener> getHandshakeCompletedListeners() {
        return this.handshakeCompletedListeners;
    }

    public void setHandshakeCompletedListeners(Set<HandshakeCompletedListener> handshakeCompletedListeners) {
        this.handshakeCompletedListeners = handshakeCompletedListeners;
    }
}
