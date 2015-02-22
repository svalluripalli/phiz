package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils;
import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils.PhizMethodAdvisor;
import gov.hhs.onc.phiz.crypto.impl.AbstractPhizCryptoFactoryBean;
import java.security.SecureRandom;
import java.security.Security;
import java.util.stream.Stream;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import org.aopalliance.intercept.MethodInterceptor;

public class PhizSslContextFactoryBean extends AbstractPhizCryptoFactoryBean<SSLContext> {
    private final static String SSL_CONTEXT_SERVICE_TYPE = SSLContext.class.getSimpleName();

    private final static String BEGIN_HANDSHAKE_METHOD_NAME = "beginHandshake";
    private final static String ENGINE_CREATE_SSL_ENGINE_METHOD_NAME = "engineCreateSSLEngine";

    private KeyManager[] keyManagers;
    private SecureRandom secureRandom;
    private TrustManager[] trustManagers;

    public PhizSslContextFactoryBean() {
        super(SSLContext.class);
    }

    @Override
    public SSLContext getObject() throws Exception {
        SSLContextSpi contextSpi =
            ((SSLContextSpi) Stream.of(Security.getProviders()).flatMap(prov -> prov.getServices().stream())
                .filter(service -> (service.getType().equals(SSL_CONTEXT_SERVICE_TYPE) && service.getAlgorithm().equals(this.type))).findFirst().get()
                .newInstance(null));

        return new SSLContext(PhizProxyUtils.buildProxyFactory(contextSpi, SSLContextSpi.class,
            new PhizMethodAdvisor(((MethodInterceptor) contextInvocation -> {
                SSLEngine engine = ((SSLEngine) contextInvocation.proceed());

                return PhizProxyUtils.buildProxyFactory(engine, SSLEngine.class, new PhizMethodAdvisor(((MethodInterceptor) engineInvocation -> {
                    SSLSession session = engine.getSession();

                    if (session.isValid()) {
                        session.invalidate();
                    }

                    engineInvocation.proceed();

                    return null;
                }), BEGIN_HANDSHAKE_METHOD_NAME)).getProxy();
            }), ENGINE_CREATE_SSL_ENGINE_METHOD_NAME)).getProxy(), this.prov, this.type) {
            {
                this.init(PhizSslContextFactoryBean.this.keyManagers, PhizSslContextFactoryBean.this.trustManagers, PhizSslContextFactoryBean.this.secureRandom);
            }
        };
    }

    public KeyManager[] getKeyManagers() {
        return this.keyManagers;
    }

    public void setKeyManagers(KeyManager[] keyManagers) {
        this.keyManagers = keyManagers;
    }

    public SecureRandom getSecureRandom() {
        return this.secureRandom;
    }

    public void setSecureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    public TrustManager[] getTrustManagers() {
        return this.trustManagers;
    }

    public void setTrustManagers(TrustManager[] trustManagers) {
        this.trustManagers = trustManagers;
    }
}
