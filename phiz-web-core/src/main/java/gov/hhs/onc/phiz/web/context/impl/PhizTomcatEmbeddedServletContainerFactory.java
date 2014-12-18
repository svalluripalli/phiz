package gov.hhs.onc.phiz.web.context.impl;

import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.tomcat.util.net.SSLImplementation;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;

public class PhizTomcatEmbeddedServletContainerFactory extends TomcatEmbeddedServletContainerFactory {
    private Class<? extends SSLImplementation> sslImplClass;

    public PhizTomcatEmbeddedServletContainerFactory(String contextPath, int port) {
        super(contextPath, port);
    }

    @Override
    protected void configureSsl(AbstractHttp11JsseProtocol<?> protocol, Ssl ssl) {
        // TEMP: dev
        super.configureSsl(protocol, ssl);

        protocol.setSslImplementationName(this.sslImplClass.getName());
    }

    public Class<? extends SSLImplementation> getSslImplementationClass() {
        return this.sslImplClass;
    }

    public void setSslImplementationClass(Class<? extends SSLImplementation> sslImplClass) {
        this.sslImplClass = sslImplClass;
    }
}
