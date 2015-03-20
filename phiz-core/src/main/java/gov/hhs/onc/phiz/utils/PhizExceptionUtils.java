package gov.hhs.onc.phiz.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public final class PhizExceptionUtils {
    private PhizExceptionUtils() {
    }

    public static Throwable getRootCause(Throwable throwable) {
        return ObjectUtils.defaultIfNull(ExceptionUtils.getRootCause(throwable), throwable);
    }
}
