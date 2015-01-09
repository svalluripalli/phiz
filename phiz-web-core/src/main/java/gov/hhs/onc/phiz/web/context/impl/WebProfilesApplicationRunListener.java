package gov.hhs.onc.phiz.web.context.impl;

import gov.hhs.onc.phiz.context.PhizProfiles;
import gov.hhs.onc.phiz.context.impl.AbstractPhizApplicationRunListener;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.WebApplicationContext;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class WebProfilesApplicationRunListener extends AbstractPhizApplicationRunListener {
    public WebProfilesApplicationRunListener(SpringApplication app, String[] args) {
        super(app, args);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext appContext) {
        if (appContext instanceof WebApplicationContext) {
            appContext.getEnvironment().addActiveProfile(PhizProfiles.WEB_CONTEXT);
        }
    }
}
