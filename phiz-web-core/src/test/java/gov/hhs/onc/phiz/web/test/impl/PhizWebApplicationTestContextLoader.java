package gov.hhs.onc.phiz.web.test.impl;

import gov.hhs.onc.phiz.context.impl.PhizApplication;
import gov.hhs.onc.phiz.test.impl.PhizApplicationTestContextLoader;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;

public class PhizWebApplicationTestContextLoader extends PhizApplicationTestContextLoader {
    @Override
    protected PhizApplication getSpringApplication() {
        PhizApplication app = super.getSpringApplication();
        app.setApplicationContextClass(AnnotationConfigEmbeddedWebApplicationContext.class);
        app.setWebEnvironment(true);

        return app;
    }
}
