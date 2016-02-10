/*====================================================================================================
= LOGGERS: APACHE TOMCAT
=====================================================================================================*/
logger("org.apache.catalina", INFO, [ "console", "file", "logstashFile" ], false)

logger("org.apache.coyote", INFO, [ "console", "file", "logstashFile" ], false)

logger("org.apache.tomcat", INFO, [ "console", "file", "logstashFile" ], false)
