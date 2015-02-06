package gov.hhs.onc.phiz.context.utils;

import gov.hhs.onc.phiz.context.PhizProperties;
import org.apache.commons.lang3.BooleanUtils;

public final class PhizApplicationUtils {
    private PhizApplicationUtils() {
    }

    public static boolean isDaemon() {
        return (BooleanUtils.toBoolean(System.getProperty(PhizProperties.WRAPPER_DAEMON_NAME)) || (System.console() == null));
    }
}
