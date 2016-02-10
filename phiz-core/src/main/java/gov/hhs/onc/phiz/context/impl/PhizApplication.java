package gov.hhs.onc.phiz.context.impl;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

public class PhizApplication extends SpringApplication {
    public final static String BEAN_NAME = "app";

    private CompositePropertySource propSrc;

    public PhizApplication(Object ... srcs) {
        super(srcs);
    }

    @Override
    protected void postProcessApplicationContext(ConfigurableApplicationContext appContext) {
        super.postProcessApplicationContext(appContext);

        ((GenericApplicationContext) appContext).getBeanFactory().registerSingleton(BEAN_NAME, this);
    }

    @Override
    protected void configurePropertySources(ConfigurableEnvironment env, String[] args) {
        super.configurePropertySources(env, args);

        env.getPropertySources().addLast(this.propSrc);
    }

    public CompositePropertySource getPropertySource() {
        return this.propSrc;
    }

    public void setPropertySource(CompositePropertySource propSrc) {
        this.propSrc = propSrc;
    }
}
