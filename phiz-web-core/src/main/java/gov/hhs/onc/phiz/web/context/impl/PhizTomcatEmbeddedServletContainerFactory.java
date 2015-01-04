package gov.hhs.onc.phiz.web.context.impl;

import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.tomcat.util.net.SSLImplementation;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;

public class PhizTomcatEmbeddedServletContainerFactory extends TomcatEmbeddedServletContainerFactory {
    private final static Ssl SSL_PLACEHOLDER_INSTANCE = new Ssl();
    
    private Class<? extends SSLImplementation> sslImplClass;
    
    {
        this.setSsl(SSL_PLACEHOLDER_INSTANCE);
    }
    
    @Override
    protected void configureSsl(AbstractHttp11JsseProtocol<?> protocol, Ssl ssl) {
        protocol.setSSLEnabled(true);
        protocol.setSslImplementationName(this.sslImplClass.getName());
    }

    public Class<? extends SSLImplementation> getSslImplementationClass() {
        return this.sslImplClass;
    }

    public void setSslImplementationClass(Class<? extends SSLImplementation> sslImplClass) {
        this.sslImplClass = sslImplClass;
    }
}
