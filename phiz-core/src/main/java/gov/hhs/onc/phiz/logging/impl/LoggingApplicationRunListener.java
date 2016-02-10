package gov.hhs.onc.phiz.logging.impl;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.helpers.NOPAppender;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.StatusPrinter;
import gov.hhs.onc.phiz.context.PhizProfiles;
import gov.hhs.onc.phiz.context.PhizProperties;
import gov.hhs.onc.phiz.context.impl.AbstractPhizApplicationRunListener;
import gov.hhs.onc.phiz.utils.PhizResourceUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingApplicationRunListener extends AbstractPhizApplicationRunListener {
    public final static String ENV_OBJ_NAME = "env";

    private final static String CONFIG_FILE_RESOURCE_LOC_PATTERN =
        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "META-INF/phiz/logback/logback-phiz*.groovy";

    private Logger logger;

    public LoggingApplicationRunListener(SpringApplication app, String[] args) {
        super(app, args);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext appContext) {
        ConfigurableEnvironment env = appContext.getEnvironment();

        this.processProfile(env, PhizProperties.LOGGING_LOGSTASH_FILE_ENABLED_NAME, PhizProfiles.LOGGING_LOGSTASH_FILE);
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment env) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        LoggerContext loggerContext = ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext();
        loggerContext.stop();
        loggerContext.reset();

        LevelChangePropagator lvlChangePropagator = new LevelChangePropagator();
        lvlChangePropagator.setContext(loggerContext);
        lvlChangePropagator.setResetJUL(true);
        loggerContext.addListener(lvlChangePropagator);

        NOPAppender<ILoggingEvent> nopAppender = new NOPAppender<>();
        nopAppender.setContext(loggerContext);
        nopAppender.start();

        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(nopAppender);

        Resource[] configFileResources;

        try {
            configFileResources = ResourcePatternUtils.getResourcePatternResolver(this.app.getResourceLoader()).getResources(CONFIG_FILE_RESOURCE_LOC_PATTERN);
        } catch (IOException e) {
            throw new ApplicationContextException(
                String.format("Unable to resolve Logback configuration file resource(s) for pattern: %s", CONFIG_FILE_RESOURCE_LOC_PATTERN), e);
        }

        Arrays.sort(configFileResources, PhizResourceUtils.LOC_COMPARATOR);

        StrBuilder configContentBuilder = new StrBuilder();
        configContentBuilder.setNewLineText(StringUtils.LF);

        for (Resource configFileResource : configFileResources) {
            try (InputStream configFileInStream = configFileResource.getInputStream()) {
                configContentBuilder.append(IOUtils.toString(configFileInStream, StandardCharsets.UTF_8));

                configContentBuilder.appendNewLine();
            } catch (IOException e) {
                throw new ApplicationContextException(String.format("Unable to read Logback configuration file resource (fileName=%s, desc=%s).",
                    configFileResource.getFilename(), configFileResource.getDescription()), e);
            }
        }

        loggerContext.putObject(ENV_OBJ_NAME, env);

        GafferConfigurator gafferConfigurator = new GafferConfigurator(loggerContext);

        loggerContext.putObject(ClassicConstants.GAFFER_CONFIGURATOR_FQCN, gafferConfigurator);

        gafferConfigurator.run(configContentBuilder.build());

        StatusManager statusManager = loggerContext.getStatusManager();
        StatusUtil statusUtil = new StatusUtil(statusManager);
        long lastResetTime = statusUtil.timeOfLastReset();

        if (statusUtil.getHighestLevel(lastResetTime) >= Status.WARN) {
            StatusPrinter.print(statusManager, lastResetTime);

            throw new ApplicationContextException(
                String.format("Unable to initialize Logback using configuration file resource(s) for pattern: %s", CONFIG_FILE_RESOURCE_LOC_PATTERN));
        }

        rootLogger.detachAppender(nopAppender);

        (this.logger = loggerContext.getLogger(LoggingApplicationRunListener.class))
            .info(String.format("Logback initialized using configuration file resource(s) for pattern: %s", CONFIG_FILE_RESOURCE_LOC_PATTERN));
    }

    private void processProfile(ConfigurableEnvironment env, String propName, String profileName) {
        if (!BooleanUtils.toBoolean(env.getProperty(propName))) {
            return;
        }

        env.addActiveProfile(profileName);

        this.logger.info(String.format("Activated logging Spring profile (name=%s).", profileName));
    }
}
