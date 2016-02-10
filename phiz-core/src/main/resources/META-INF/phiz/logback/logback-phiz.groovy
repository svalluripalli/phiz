import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.helpers.NOPAppender
import ch.qos.logback.core.status.NopStatusListener
import ch.qos.logback.ext.spring.DelegatingLogbackAppender
import gov.hhs.onc.phiz.context.PhizProperties
import gov.hhs.onc.phiz.logging.impl.EventIdMdcConverter
import gov.hhs.onc.phiz.logging.impl.LoggingApplicationRunListener
import gov.hhs.onc.phiz.logging.impl.PriorityColorCompositeConverter
import gov.hhs.onc.phiz.logging.impl.RootCauseThrowableProxyConverter
import gov.hhs.onc.phiz.logging.metrics.impl.LogstashReporter
import java.nio.charset.StandardCharsets
import org.apache.commons.lang3.BooleanUtils
import org.apache.cxf.binding.soap.interceptor.Soap12FaultOutInterceptor
import org.springframework.core.env.ConfigurableEnvironment

/*====================================================================================================
= CONTEXT VARIABLES
=====================================================================================================*/
def env = ((ConfigurableEnvironment) context.getObject(LoggingApplicationRunListener.ENV_OBJ_NAME))

/*====================================================================================================
= CONVERSION RULES
=====================================================================================================*/
conversionRule("exRoot", RootCauseThrowableProxyConverter)

conversionRule("pColor", PriorityColorCompositeConverter)

conversionRule("xEventId", EventIdMdcConverter)

/*====================================================================================================
= STATUS LISTENERS
=====================================================================================================*/
statusListener(NopStatusListener)

/*====================================================================================================
= APPENDER: CONSOLE
=====================================================================================================*/
if (BooleanUtils.toBoolean(env.getProperty(PhizProperties.LOGGING_CONSOLE_ENABLED_NAME))) {
    appender("console", ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            charset = StandardCharsets.UTF_8
            pattern = "%pColor - %m%n%exRoot"
        }
        target = "System.out"
        withJansi = true
    }
} else {
    appender("console", NOPAppender)
}

/*====================================================================================================
= APPENDER: FILE
=====================================================================================================*/
if (BooleanUtils.toBoolean(env.getProperty(PhizProperties.LOGGING_FILE_ENABLED_NAME))) {
    appender("file", FileAppender) {
        encoder(PatternLayoutEncoder) {
            charset = StandardCharsets.UTF_8
            pattern = "%d{yyyy-MM-dd HH:mm:ss z} [%C:%L %t]%xEventId %p - %m%n%exRoot"
        }
        file = "${env.getProperty(PhizProperties.LOGGING_FILE_DIR_NAME)}/${env.getProperty(PhizProperties.LOGGING_FILE_NAME_NAME)}.log"
        prudent = true
    }
} else {
    appender("file", NOPAppender)
}

/*====================================================================================================
= APPENDER: LOGSTASH FILE
=====================================================================================================*/
if (BooleanUtils.toBoolean(env.getProperty(PhizProperties.LOGGING_LOGSTASH_FILE_ENABLED_NAME))) {
    appender("logstashFile", DelegatingLogbackAppender) {
        beanName = "appenderFileLogstash"
    }
} else {
    appender("logstashFile", NOPAppender)
}

/*====================================================================================================
= LOGGERS: PROJECT
=====================================================================================================*/
logger("gov.hhs.onc.phiz", ALL, [ "console", "file", "logstashFile" ], false)

logger(LogstashReporter.name, INFO, [ "logstashFile" ], false)

/*====================================================================================================
= LOGGERS: ROCKFRAMEWORK
=====================================================================================================*/
logger("br.net.woodstock.rockframework", INFO, [ "console", "file", "logstashFile" ], false)

/*====================================================================================================
= LOGGERS: DROPWIZARD METRICS
=====================================================================================================*/
logger("com.codahale.metrics", INFO, [ "console", "file", "logstashFile" ], false)

/*====================================================================================================
= LOGGERS: APACHE
=====================================================================================================*/
logger("org.apache", INFO, [ "console", "file", "logstashFile" ], false)

/*====================================================================================================
= LOGGERS: APACHE
=====================================================================================================*/
logger("org.apache.cxf", INFO, [ "console", "file", "logstashFile" ], false)

logger(Soap12FaultOutInterceptor.name, WARN, ["console", "file", "logstashFile" ], false)

/*====================================================================================================
= LOGGERS: HIBERNATE
=====================================================================================================*/
logger("org.hibernate", INFO, [ "console", "file", "logstashFile" ], false)

//logger("org.hibernate.SQL", DEBUG, [ "console", "file", "logstashFile" ], false)

//logger("org.hibernate.type", TRACE, [ "console", "file", "logstashFile" ], false)

/*====================================================================================================
= LOGGERS: SPRING FRAMEWORK
=====================================================================================================*/
logger("org.springframework", INFO, [ "console", "file", "logstashFile" ], false)

logger("org.springframework.context.support.PostProcessorRegistrationDelegate\$BeanPostProcessorChecker", WARN, [ "console", "file", "logstashFile" ], false)

/*====================================================================================================
= ROOT LOGGER
=====================================================================================================*/
root(WARN, [ "console", "file", "logstashFile" ])
