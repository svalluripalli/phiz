package gov.hhs.onc.phiz.web.tomcat.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.net.SSLImplementation;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

public class PhizTomcatEmbeddedServletContainerFactory extends TomcatEmbeddedServletContainerFactory {
    public static class PhizRequestFacade extends RequestFacade {
        private PhizRequest req;

        public PhizRequestFacade(PhizRequest req) {
            super(req);

            this.req = req;
        }

        public PhizRequest getRequest() {
            return this.req;
        }
    }

    public static class PhizRequest extends Request {
        @Override
        public HttpServletRequest getRequest() {
            return ((this.facade != null) ? this.facade : (this.facade = new PhizRequestFacade(this)));
        }
    }

    public static class PhizResponseFacade extends ResponseFacade {
        private PhizResponse resp;

        public PhizResponseFacade(PhizResponse resp) {
            super(resp);

            this.resp = resp;
        }

        public PhizResponse getResponse() {
            return this.resp;
        }
    }

    public static class PhizResponse extends Response {
        @Override
        public HttpServletResponse getResponse() {
            return ((this.facade != null) ? this.facade : (this.facade = new PhizResponseFacade(this)));
        }
    }

    public static class PhizConnector extends Connector {
        public PhizConnector() {
            super(Http11NioProtocol.class.getName());
        }

        @Override
        public Response createResponse() {
            PhizResponse resp = new PhizResponse();
            resp.setConnector(this);

            return resp;
        }

        @Override
        public Request createRequest() {
            PhizRequest req = new PhizRequest();
            req.setConnector(this);

            return req;
        }
    }

    private final static Ssl SSL_PLACEHOLDER_INSTANCE = new Ssl();

    private File baseDir;
    private int connTimeout;
    private int maxConns;
    private int maxConnThreads;
    private LoginConfig loginConfig;
    private PhizSessionConfig sessionConfig;
    private Class<? extends SSLImplementation> sslImplClass;
    private Tomcat tomcat;
    private PhizConnector conn;

    {
        this.setSsl(SSL_PLACEHOLDER_INSTANCE);
    }

    @Override
    public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer ... servletContextInits) {
        (this.tomcat = new Tomcat()).setBaseDir(this.baseDir.getAbsolutePath());

        Service service = this.tomcat.getService();
        service.addConnector((this.conn = new PhizConnector()));

        this.customizeConnector(this.conn);
        this.tomcat.setConnector(this.conn);

        Host host = this.tomcat.getHost();
        host.setAutoDeploy(false);

        this.tomcat.getEngine().setBackgroundProcessorDelay(-1);

        this.getAdditionalTomcatConnectors().forEach(service::addConnector);

        this.prepareContext(host, servletContextInits);

        return this.getTomcatEmbeddedServletContainer(this.tomcat);
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

    public File getBaseDirectory() {
        return this.baseDir;
    }

    @Override
    public void setBaseDirectory(File baseDir) {
        super.setBaseDirectory((this.baseDir = baseDir));
    }

    public int getConnectionTimeout() {
        return this.connTimeout;
    }

    public void setConnectionTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public PhizConnector getConnector() {
        return this.conn;
    }

    @Override
    public void setContextValves(Collection<? extends Valve> contextValves) {
        super.setContextValves(((contextValves.size() > 1) ? contextValves.stream().sorted(AnnotationAwareOrderComparator.INSTANCE)
            .collect(Collectors.toCollection(ArrayList<Valve>::new)) : contextValves));
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

    public Tomcat getTomcat() {
        return this.tomcat;
    }
}
