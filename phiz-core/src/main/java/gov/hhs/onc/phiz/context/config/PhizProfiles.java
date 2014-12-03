package gov.hhs.onc.phiz.context.config;

public final class PhizProfiles {
    private final static String PREFIX = "phiz.profile.";
    private final static String MODE_PREFIX = PREFIX + "mode.";

    public final static String DEV_MODE = MODE_PREFIX + "dev";
    public final static String PROD_MODE = MODE_PREFIX + "prod";

    private PhizProfiles() {
    }
}
