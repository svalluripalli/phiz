package gov.hhs.onc.phiz.web.test.impl;

import gov.hhs.onc.phiz.test.impl.PhizApplicationTestContextLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.XmlEmbeddedWebApplicationContext;

public class PhizWebApplicationTestContextLoader extends PhizApplicationTestContextLoader {
    @Override
    protected SpringApplication getSpringApplication() {
        SpringApplication app = super.getSpringApplication();
        app.setApplicationContextClass(XmlEmbeddedWebApplicationContext.class);
        app.setWebEnvironment(true);

        return app;
    }
}
