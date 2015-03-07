package gov.hhs.onc.phiz.utils;

import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;

public final class PhizStringUtils {
    public final static String UNDERSCORE = "_";

    private PhizStringUtils() {
    }

    public static String joinCamelCase(String ... strParts) {
        for (int a = 0; a < strParts.length; a++) {
            strParts[a] = strParts[a].toLowerCase();

            if (a > 0) {
                strParts[a] = StringUtils.capitalize(strParts[a]);
            }
        }

        return StringUtils.join(strParts, StringUtils.EMPTY);
    }

    public static String[] tokenize(@Nullable String str) {
        return tokenize(str, null);
    }

    public static String[] tokenize(@Nullable String str, @Nullable String defaultStr) {
        return ObjectUtils.defaultIfNull(org.springframework.util.StringUtils.tokenizeToStringArray(ObjectUtils.defaultIfNull(str, defaultStr),
            ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS), ArrayUtils.EMPTY_STRING_ARRAY);
    }
}
