package gov.hhs.onc.phiz.context;

public final class PhizProperties {
    public final static String PREFIX = "phiz.";
    public final static String APP_PREFIX = PREFIX + "app.";
    public final static String LOGGING_PREFIX = PREFIX + "logging.";
    public final static String FILE_LOGGING_PREFIX = LOGGING_PREFIX + "file.";
    public final static String WRAPPER_PREFIX = "wrapper.";

    public final static String ENABLED_SUFFIX = "enabled";

    public final static String APP_NAME_NAME = APP_PREFIX + "name";
    public final static String APP_PID_NAME = APP_PREFIX + "pid";

    public final static String FILE_LOGGING_NAME = FILE_LOGGING_PREFIX + ENABLED_SUFFIX;
    public final static String LOGSTASH_FILE_LOGGING_NAME = FILE_LOGGING_PREFIX + "logstash." + ENABLED_SUFFIX;

    public final static String MODE_NAME = PREFIX + "mode";
    public final static String DEV_MODE_VALUE = "dev";
    public final static String PROD_MODE_VALUE = "prod";

    public final static String WRAPPER_DAEMON_NAME = WRAPPER_PREFIX + "daemon";

    private PhizProperties() {
    }
}
