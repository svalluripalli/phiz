package gov.hhs.onc.phiz.env;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.commons.lang3.StringUtils;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface CommandLineOptionArgument {
    public String description() default StringUtils.EMPTY;

    public boolean required() default true;

    public Class<?> type() default CommandLineOptionArgument.class;
    
    public String value() default StringUtils.EMPTY;
}
