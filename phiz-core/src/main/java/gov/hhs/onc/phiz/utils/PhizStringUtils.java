package gov.hhs.onc.phiz.utils;

import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

public final class PhizStringUtils {
    public final static String TOKEN_DELIMS = ",; \t\n";

    private PhizStringUtils() {
    }

    public static String[] tokenize(@Nullable String str) {
        return tokenize(str, null);
    }

    public static String[] tokenize(@Nullable String str, @Nullable String defaultStr) {
        return ObjectUtils.defaultIfNull(org.springframework.util.StringUtils.tokenizeToStringArray(ObjectUtils.defaultIfNull(str, defaultStr), TOKEN_DELIMS),
            ArrayUtils.EMPTY_STRING_ARRAY);
    }
}
