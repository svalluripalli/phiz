package gov.hhs.onc.phiz.logging.impl;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.StatusPrinter;
import gov.hhs.onc.phiz.context.PhizProfiles;
import gov.hhs.onc.phiz.context.PhizProperties;
import gov.hhs.onc.phiz.context.impl.AbstractPhizApplicationRunListener;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingApplicationRunListener extends AbstractPhizApplicationRunListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(LoggingApplicationRunListener.class);

    public LoggingApplicationRunListener(SpringApplication app, String[] args) {
        super(app, args);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext appContext) {
        ConfigurableEnvironment env = appContext.getEnvironment();
        LoggerContext context = ContextSelectorStaticBinder.getSingleton().getContextSelector().getLoggerContext();

        processProfile(env, PhizProfiles.FILE_LOGGING, context, PhizProperties.FILE_LOGGING_NAME);
        processProfile(env, PhizProfiles.LOGSTASH_FILE_LOGGING, context, PhizProperties.LOGSTASH_FILE_LOGGING_NAME);
    }

    @Override
    public void started() {
        StatusManager statusManager = ContextSelectorStaticBinder.getSingleton().getContextSelector().getLoggerContext().getStatusManager();
        StatusUtil statusUtil = new StatusUtil(statusManager);
        long lastResetTime = statusUtil.timeOfLastReset();

        if (statusUtil.getHighestLevel(lastResetTime) >= Status.WARN) {
            StatusPrinter.print(statusManager, lastResetTime);

            throw new ApplicationContextException("Logback status manager contains warning(s) and/or error(s).");
        }
    }

    private static void processProfile(ConfigurableEnvironment env, String profileName, LoggerContext context, String contextPropName) {
        if (!BooleanUtils.toBoolean(context.getProperty(contextPropName))) {
            return;
        }

        env.addActiveProfile(profileName);

        LOGGER.info(String.format("Activated logging Spring profile (name=%s).", profileName));
    }
}
