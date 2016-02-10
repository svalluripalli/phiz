package gov.hhs.onc.phiz.context;

public final class PhizProperties {
    public final static String PREFIX = "phiz.";
    public final static String APP_PREFIX = PREFIX + "app.";
    public final static String FILE_PREFIX = "file.";
    public final static String LOGGING_PREFIX = PREFIX + "logging.";
    public final static String LOGGING_CONSOLE_PREFIX = LOGGING_PREFIX + "console.";
    public final static String LOGGING_FILE_PREFIX = LOGGING_PREFIX + FILE_PREFIX;
    public final static String LOGGING_LOGSTASH_FILE_PREFIX = LOGGING_PREFIX + "logstash." + FILE_PREFIX;
    public final static String WRAPPER_PREFIX = "wrapper.";

    public final static String DIR_SUFFIX = "dir";
    public final static String ENABLED_SUFFIX = "enabled";
    public final static String NAME_SUFFIX = "name";

    public final static String APP_NAME_NAME = APP_PREFIX + NAME_SUFFIX;
    public final static String APP_PID_NAME = APP_PREFIX + "pid";

    public final static String LOGGING_CONSOLE_ENABLED_NAME = LOGGING_CONSOLE_PREFIX + ENABLED_SUFFIX;
    public final static String LOGGING_CONSOLE_TTY_NAME = LOGGING_CONSOLE_PREFIX + "tty";
    
    public final static String LOGGING_FILE_DIR_NAME = LOGGING_FILE_PREFIX + DIR_SUFFIX;
    public final static String LOGGING_FILE_ENABLED_NAME = LOGGING_FILE_PREFIX + ENABLED_SUFFIX;
    public final static String LOGGING_FILE_NAME_NAME = LOGGING_FILE_PREFIX + NAME_SUFFIX;
    
    public final static String LOGGING_LOGSTASH_FILE_DIR_NAME = LOGGING_LOGSTASH_FILE_PREFIX + DIR_SUFFIX;
    public final static String LOGGING_LOGSTASH_FILE_ENABLED_NAME = LOGGING_LOGSTASH_FILE_PREFIX + ENABLED_SUFFIX;
    public final static String LOGGING_LOGSTASH_FILE_NAME_NAME = LOGGING_LOGSTASH_FILE_PREFIX + NAME_SUFFIX;

    public final static String MODE_NAME = PREFIX + "mode";
    public final static String DEV_MODE_VALUE = "dev";
    public final static String PROD_MODE_VALUE = "prod";

    public final static String WRAPPER_DAEMON_NAME = WRAPPER_PREFIX + "daemon";

    private PhizProperties() {
    }
}
