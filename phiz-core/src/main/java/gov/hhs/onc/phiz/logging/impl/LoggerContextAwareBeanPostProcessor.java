package gov.hhs.onc.phiz.logging.impl;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextAware;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.spi.ContextAware;
import gov.hhs.onc.phiz.beans.factory.impl.AbstractPhizBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("beanPostProcLoggerContextAware")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggerContextAwareBeanPostProcessor extends AbstractPhizBeanPostProcessor<ContextAware> {
    private LoggerContext loggerContext = ContextSelectorStaticBinder.getSingleton().getContextSelector().getLoggerContext();

    public LoggerContextAwareBeanPostProcessor() {
        super(ContextAware.class);
    }

    @Override
    protected ContextAware postProcessBeforeInitializationInternal(ContextAware bean, String beanName) throws Exception {
        bean.setContext(this.loggerContext);

        if (bean instanceof LoggerContextAware) {
            ((LoggerContextAware) bean).setLoggerContext(this.loggerContext);
        }

        return bean;
    }
}
