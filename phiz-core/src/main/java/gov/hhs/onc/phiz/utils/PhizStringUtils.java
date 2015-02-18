package gov.hhs.onc.phiz.utils;

import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ConfigurableApplicationContext;

public final class PhizStringUtils {
    private PhizStringUtils() {
    }

    public static String[] tokenize(@Nullable String str) {
        return tokenize(str, null);
    }

    public static String[] tokenize(@Nullable String str, @Nullable String defaultStr) {
        return ObjectUtils.defaultIfNull(org.springframework.util.StringUtils.tokenizeToStringArray(ObjectUtils.defaultIfNull(str, defaultStr),
            ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS), ArrayUtils.EMPTY_STRING_ARRAY);
    }
}
