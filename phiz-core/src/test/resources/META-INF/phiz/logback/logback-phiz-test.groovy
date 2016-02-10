import org.springframework.test.context.support.AbstractContextLoader
import org.springframework.test.context.support.DefaultTestContextBootstrapper

/*====================================================================================================
= LOGGERS: SPRING FRAMEWORK
=====================================================================================================*/
logger(AbstractContextLoader.name, WARN, ["console", "file", "logstashFile" ], false)

logger(DefaultTestContextBootstrapper.name, WARN, ["console", "file", "logstashFile" ], false)
