package gov.hhs.onc.phiz.test.impl;

import gov.hhs.onc.phiz.context.impl.PhizApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.SpringApplicationContextLoader;

public class PhizApplicationTestContextLoader extends SpringApplicationContextLoader {
    @Override
    protected SpringApplication getSpringApplication() {
        SpringApplication app = PhizApplication.buildApplication();
        app.setWebEnvironment(false);

        return app;
    }
}
