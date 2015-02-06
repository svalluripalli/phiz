package gov.hhs.onc.phiz.env.impl;

import gov.hhs.onc.phiz.context.impl.AbstractPhizApplicationRunListener;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class CommandLineApplicationRunListener extends AbstractPhizApplicationRunListener {
    public final static String ARGS_BEAN_NAME = "cmdLineArgs";

    public CommandLineApplicationRunListener(SpringApplication app, String[] args) {
        super(app, args);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext appContext) {
        appContext.getBeanFactory().registerSingleton(ARGS_BEAN_NAME, this.args);
    }
}
