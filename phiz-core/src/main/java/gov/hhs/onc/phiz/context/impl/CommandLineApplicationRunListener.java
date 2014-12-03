package gov.hhs.onc.phiz.context.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;

public class CommandLineApplicationRunListener extends AbstractPhizApplicationRunListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(CommandLineApplicationRunListener.class);

    public CommandLineApplicationRunListener(SpringApplication app, String[] args) {
        super(app, args);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext appContext) {
        CommandLinePropertySource<?> argsPropSrc = new SimpleCommandLinePropertySource(this.args);
        String argsPropSrcName = argsPropSrc.getName();

        appContext.getBeanFactory().registerSingleton(argsPropSrcName, argsPropSrc);

        LOGGER.info(String.format("Registered command line properties (num=%d) source (name=%s, class=%s).", argsPropSrc.getPropertyNames().length,
            argsPropSrcName, argsPropSrc.getClass().getName()));
    }
}
