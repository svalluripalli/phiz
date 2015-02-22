package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils;
import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils.PhizMethodAdvisor;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

public class PhizSslClientSocketFactoryFactoryBean extends AbstractPhizSslSocketFactoryFactoryBean<SSLSocketFactory, SSLSocket> {
    private final static String CREATE_SOCKET_METHOD_NAME = "createSocket";

    public PhizSslClientSocketFactoryFactoryBean() {
        super(SSLSocketFactory.class);
    }

    @Override
    protected AspectJProxyFactory buildProxyFactory() {
        return PhizProxyUtils.buildProxyFactory(this.sslContext.getSocketFactory(), this.objClass, new PhizMethodAdvisor(((MethodInterceptor) invocation -> {
            SSLSocket socket = ((SSLSocket) invocation.proceed());
            socket.setSSLParameters(this.sslParams);

            return socket;
        }), CREATE_SOCKET_METHOD_NAME));
    }
}
