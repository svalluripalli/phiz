package gov.hhs.onc.phiz.test.impl;

import gov.hhs.onc.phiz.context.impl.PhizApplication;
import gov.hhs.onc.phiz.context.impl.PhizApplicationConfiguration;
import org.springframework.boot.test.SpringApplicationContextLoader;

public class PhizApplicationTestContextLoader extends SpringApplicationContextLoader {
    @Override
    protected PhizApplication getSpringApplication() {
        PhizApplication app = PhizApplicationConfiguration.buildApplication();
        app.setWebEnvironment(false);

        return app;
    }
}
