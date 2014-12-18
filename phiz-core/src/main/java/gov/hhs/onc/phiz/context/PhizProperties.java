package gov.hhs.onc.phiz.context;

public final class PhizProperties {
    public final static String PREFIX = "phiz.";
    public final static String APP_PREFIX = PREFIX + "app.";

    public final static String APP_NAME_NAME = APP_PREFIX + "name";
    public final static String APP_PID_NAME = APP_PREFIX + "pid";

    public final static String MODE_NAME = PREFIX + "mode";
    public final static String DEV_MODE_VALUE = "dev";
    public final static String PROD_MODE_VALUE = "prod";

    private PhizProperties() {
    }
}
