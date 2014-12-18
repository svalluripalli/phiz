package gov.hhs.onc.phiz.web.crypto.impl;

import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable("sslImplJssePhiz")
public class PhizJsseImplementation extends JSSEImplementation implements InitializingBean {
    private final static String IMPL_NAME = "PHIZ JSSE";

    private final static Logger LOGGER = LoggerFactory.getLogger(PhizJsseImplementation.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        // TEMP: dev
        LOGGER.warn(String.format("%s.afterPropertiesSet()", this.getClass().getSimpleName()));
    }

    @Override
    public String getImplementationName() {
        return IMPL_NAME;
    }
}
