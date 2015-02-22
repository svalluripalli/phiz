package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils;
import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils.PhizMethodAdvisor;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

public class PhizSslServerSocketFactoryFactoryBean extends AbstractPhizSslSocketFactoryFactoryBean<SSLServerSocketFactory, SSLServerSocket> {
    private final static String CREATE_SERVER_SOCKET_METHOD_NAME = "createServerSocket";

    public PhizSslServerSocketFactoryFactoryBean() {
        super(SSLServerSocketFactory.class);
    }

    @Override
    protected AspectJProxyFactory buildProxyFactory() {
        return PhizProxyUtils.buildProxyFactory(this.sslContext.getServerSocketFactory(), this.objClass, new PhizMethodAdvisor(
            ((MethodInterceptor) invocation -> {
                SSLServerSocket serverSocket = ((SSLServerSocket) invocation.proceed());
                serverSocket.setSSLParameters(this.sslParams);

                return serverSocket;
            }), CREATE_SERVER_SOCKET_METHOD_NAME));
    }
}
