package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils;
import gov.hhs.onc.phiz.aop.utils.PhizProxyUtils.PhizMethodAdvisor;
import gov.hhs.onc.phiz.crypto.utils.PhizCertificateUtils;
import gov.hhs.onc.phiz.logging.logstash.PhizLogstashTags;
import gov.hhs.onc.phiz.logging.logstash.impl.PhizLogstashMarkers;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhizSslContextFactoryBean extends AbstractPhizSslParametersAwareFactoryBean<SSLContext> {
    private final static String SSL_CONTEXT_SERVICE_TYPE = SSLContext.class.getSimpleName();

    private final static String ENGINE_CREATE_SSL_ENGINE_METHOD_NAME = "engineCreateSSLEngine";
    private final static String ENGINE_GET_SERVER_SOCKET_FACTORY_METHOD_NAME = "engineGetServerSocketFactory";
    private final static String ENGINE_GET_SOCKET_FACTORY_METHOD_NAME = "engineGetSocketFactory";

    private final static String CREATE_SERVER_SOCKET_METHOD_NAME = "createServerSocket";
    private final static String CREATE_SOCKET_METHOD_NAME = "createSocket";

    private final static String BEGIN_HANDSHAKE_METHOD_NAME = "beginHandshake";

    private final static Logger LOGGER = LoggerFactory.getLogger(PhizSslContextFactoryBean.class);

    @Resource(name = "dateFormatUtcDisplay")
    private FastDateFormat displayDateFormat;

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

        return new SSLContext(PhizProxyUtils.buildProxyFactory(
            contextSpi,
            SSLContextSpi.class,
            new PhizMethodAdvisor(((MethodInterceptor) contextInvocation -> this.buildEngine(((SSLEngine) contextInvocation.proceed()))),
                ENGINE_CREATE_SSL_ENGINE_METHOD_NAME),
            new PhizMethodAdvisor(
                ((MethodInterceptor) contextInvocation -> this.buildServerSocketFactory(((SSLServerSocketFactory) contextInvocation.proceed()))),
                ENGINE_GET_SERVER_SOCKET_FACTORY_METHOD_NAME),
            new PhizMethodAdvisor(((MethodInterceptor) contextInvocation -> this.buildSocketFactory(((SSLSocketFactory) contextInvocation.proceed()))),
                ENGINE_GET_SOCKET_FACTORY_METHOD_NAME)).getProxy(), this.prov, this.type) {
            {
                this.init(PhizSslContextFactoryBean.this.keyManagers, PhizSslContextFactoryBean.this.trustManagers, PhizSslContextFactoryBean.this.secureRandom);
            }
        };
    }

    private SSLSocketFactory buildSocketFactory(SSLSocketFactory socketFactory) {
        return PhizProxyUtils.buildProxyFactory(socketFactory, SSLSocketFactory.class, new PhizMethodAdvisor(((MethodInterceptor) socketFactoryInvocation -> {
            SSLSocket socket = ((SSLSocket) socketFactoryInvocation.proceed());
            socket.setSSLParameters(this.params);

            return socket;
        }), CREATE_SOCKET_METHOD_NAME)).getProxy();
    }

    private SSLServerSocketFactory buildServerSocketFactory(SSLServerSocketFactory serverSocketFactory) {
        return PhizProxyUtils.buildProxyFactory(serverSocketFactory, SSLServerSocketFactory.class,
            new PhizMethodAdvisor(((MethodInterceptor) serverSocketFactoryInvocation -> {
                SSLServerSocket serverSocket = ((SSLServerSocket) serverSocketFactoryInvocation.proceed());
                serverSocket.setSSLParameters(this.params);

                return serverSocket;
            }), CREATE_SERVER_SOCKET_METHOD_NAME)).getProxy();
    }

    private SSLEngine buildEngine(SSLEngine engine) {
        engine.setSSLParameters(this.params);

        return PhizProxyUtils
            .buildProxyFactory(
                engine,
                SSLEngine.class,
                new PhizMethodAdvisor(
                    ((MethodInterceptor) engineInvocation -> {
                        ExtendedSSLSession session = ((ExtendedSSLSession) engine.getSession());

                        if (session.isValid()) {
                            session.invalidate();

                            LOGGER.debug(
                                PhizLogstashMarkers.append(PhizLogstashTags.SSL),
                                String
                                    .format(
                                        "Existing SSL session (id=%s, creationTime=%s, lastAccessedTime=%s, peerHost=%s, peerPort=%d, protocol=%s, cipherSuite=%s, localCertificateDnNames=[%s], peerCertificateDnNames=[%s]) invalidated.",
                                        Hex.encodeHexString(session.getId()), this.displayDateFormat.format(new Date(session.getCreationTime())),
                                        this.displayDateFormat.format(new Date(session.getLastAccessedTime())), session.getPeerHost(), session.getPeerPort(),
                                        session.getProtocol(), session.getCipherSuite(),
                                        StringUtils.join(PhizCertificateUtils.buildSubjectDnNames(((X509Certificate[]) session.getLocalCertificates())), ", "),
                                        StringUtils.join(PhizCertificateUtils.buildSubjectDnNames(((X509Certificate[]) session.getPeerCertificates())), ", ")));
                        }

                        engineInvocation.proceed();

                        return null;
                    }), BEGIN_HANDSHAKE_METHOD_NAME)).getProxy();
    }

    public KeyManager[] getKeyManagers() {
        return this.keyManagers;
    }

    public void setKeyManagers(KeyManager ... keyManagers) {
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

    public void setTrustManagers(TrustManager ... trustManagers) {
        this.trustManagers = trustManagers;
    }
}
