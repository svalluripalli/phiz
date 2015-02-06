package gov.hhs.onc.phiz.logging.impl;

import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.StatusPrinter;
import gov.hhs.onc.phiz.context.impl.AbstractPhizApplicationRunListener;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingApplicationRunListener extends AbstractPhizApplicationRunListener {
    public LoggingApplicationRunListener(SpringApplication app, String[] args) {
        super(app, args);
    }

    @Override
    public void started() {
        StatusManager statusManager = ContextSelectorStaticBinder.getSingleton().getContextSelector().getLoggerContext().getStatusManager();
        StatusUtil statusUtil = new StatusUtil(statusManager);
        long lastResetTime = statusUtil.timeOfLastReset();

        if (statusUtil.getHighestLevel(lastResetTime) >= Status.WARN) {
            StatusPrinter.print(statusManager, lastResetTime);

            throw new ApplicationContextException(String.format("Logback status manager contains warning(s) and/or error(s)."));
        }
    }
}
