package gov.hhs.onc.phiz.context.impl;

import gov.hhs.onc.phiz.context.PhizApplicationRunListener;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public abstract class AbstractPhizApplicationRunListener implements PhizApplicationRunListener {
    protected PhizApplication app;
    protected String[] args;

    public AbstractPhizApplicationRunListener(SpringApplication app, String[] args) {
        this.app = ((PhizApplication) app);
        this.args = args;
    }

    @Override
    public void finished(ConfigurableApplicationContext appContext, Throwable exception) {
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext appContext) {
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext appContext) {
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment env) {
    }

    @Override
    public void started() {
    }
}
