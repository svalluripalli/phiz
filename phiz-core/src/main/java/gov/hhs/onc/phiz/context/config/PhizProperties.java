package gov.hhs.onc.phiz.context.config;

public final class PhizProperties {
    private final static String PREFIX = "phiz.";

    public final static String MODE_NAME = PREFIX + "mode";
    public final static String DEV_MODE_VALUE = "dev";
    public final static String PROD_MODE_VALUE = "prod";

    private PhizProperties() {
    }
}
