package gov.hhs.onc.phiz.context;

public final class PhizProfiles {
    public final static String PREFIX = "phiz.profile.";
    public final static String APP_PREFIX = PREFIX + "app.";
    public final static String CONTEXT_PREFIX = PREFIX + "context.";
    public final static String LOGGING_PREFIX = PREFIX + "logging.";
    public final static String MODE_PREFIX = PREFIX + "mode.";

    public final static String WEB_CONTEXT = CONTEXT_PREFIX + "web";

    public final static String FILE_LOGGING = LOGGING_PREFIX + "file";
    public final static String LOGSTASH_FILE_LOGGING = FILE_LOGGING + ".logstash";

    public final static String DEV_MODE = MODE_PREFIX + "dev";
    public final static String PROD_MODE = MODE_PREFIX + "prod";

    private PhizProfiles() {
    }
}
