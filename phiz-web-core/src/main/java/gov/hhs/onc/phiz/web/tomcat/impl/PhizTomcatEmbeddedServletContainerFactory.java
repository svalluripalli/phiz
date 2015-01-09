package gov.hhs.onc.phiz.web.tomcat.impl;

import java.util.Set;
import javax.servlet.SessionTrackingMode;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.net.SSLImplementation;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;

public class PhizTomcatEmbeddedServletContainerFactory extends TomcatEmbeddedServletContainerFactory {
    private final static Ssl SSL_PLACEHOLDER_INSTANCE = new Ssl();

    private int connTimeout;
    private int maxConns;
    private int maxConnThreads;
    private LoginConfig loginConfig;
    private PhizSessionConfig sessionConfig;
    private Class<? extends SSLImplementation> sslImplClass;

    {
        this.setSsl(SSL_PLACEHOLDER_INSTANCE);
    }

    @Override
    protected void customizeConnector(Connector conn) {
        super.customizeConnector(conn);

        Http11NioProtocol connProtocol = ((Http11NioProtocol) conn.getProtocolHandler());
        connProtocol.setConnectionTimeout(this.connTimeout);
        connProtocol.setMaxConnections(this.maxConns);
        connProtocol.setMaxThreads(this.maxConnThreads);
        connProtocol.setSessionCacheSize(Integer.toString(this.sessionConfig.getCacheSize()));
        connProtocol.setSessionTimeout(Integer.toString(this.sessionConfig.getTimeout()));
    }

    @Override
    protected void configureSsl(AbstractHttp11JsseProtocol<?> protocol, Ssl ssl) {
        protocol.setSSLEnabled(true);
        protocol.setSslImplementationName(this.sslImplClass.getName());
    }

    @Override
    protected void configureContext(Context context, ServletContextInitializer[] servletContextInits) {
        context.setDistributable(true);
        context.setIgnoreAnnotations(true);
        context.setTldValidation(true);
        context.setXmlNamespaceAware(true);
        context.setXmlValidation(true);
        
        context.setLoginConfig(this.loginConfig);

        super.configureContext(context, ArrayUtils.add(servletContextInits, 0, ((servletContext) -> {
            BeanUtils.copyProperties(this.sessionConfig, servletContext.getSessionCookieConfig());

            Set<SessionTrackingMode> effectiveSessionTrackingModes = servletContext.getEffectiveSessionTrackingModes();
            effectiveSessionTrackingModes.clear();
            effectiveSessionTrackingModes.addAll(this.sessionConfig.getTrackingModes());
        })));
    }

    public int getConnectionTimeout() {
        return this.connTimeout;
    }

    public void setConnectionTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public LoginConfig getLoginConfig() {
        return this.loginConfig;
    }

    public void setLoginConfig(LoginConfig loginConfig) {
        this.loginConfig = loginConfig;
    }

    public int getMaxConnections() {
        return this.maxConns;
    }

    public void setMaxConnections(int maxConns) {
        this.maxConns = maxConns;
    }

    public int getMaxConnectionThreads() {
        return this.maxConnThreads;
    }

    public void setMaxConnectionThreads(int maxConnThreads) {
        this.maxConnThreads = maxConnThreads;
    }

    public PhizSessionConfig getSessionConfig() {
        return this.sessionConfig;
    }

    public void setSessionConfig(PhizSessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    @Override
    public int getSessionTimeout() {
        return this.sessionConfig.getTimeout();
    }

    public Class<? extends SSLImplementation> getSslImplementationClass() {
        return this.sslImplClass;
    }

    public void setSslImplementationClass(Class<? extends SSLImplementation> sslImplClass) {
        this.sslImplClass = sslImplClass;
    }
}
