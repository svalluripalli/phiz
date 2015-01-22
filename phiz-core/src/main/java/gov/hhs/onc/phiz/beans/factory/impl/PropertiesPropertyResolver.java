package gov.hhs.onc.phiz.beans.factory.impl;

import com.github.sebhoss.warnings.CompilerWarnings;
import java.util.Properties;
import javax.annotation.Nullable;
import org.springframework.core.env.AbstractPropertyResolver;

public class PropertiesPropertyResolver extends AbstractPropertyResolver {
    private Properties props;

    public PropertiesPropertyResolver(Properties props) {
        this.props = props;
    }

    @Nullable
    @Override
    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    public <T> Class<T> getPropertyAsClass(String propName, Class<T> propValueClass) {
        T propValue = this.getProperty(propName, propValueClass);

        return ((propValue != null) ? ((Class<T>) propValue.getClass()) : null);
    }

    @Nullable
    @Override
    public String getProperty(String propName) {
        return this.getProperty(propName, String.class);
    }

    @Nullable
    @Override
    public <T> T getProperty(String propName, Class<T> propValueClass) {
        boolean propValueAssignable;
        Object propValue;

        return ((this.containsProperty(propName)
            && ((propValueAssignable = propValueClass.isAssignableFrom(String.class)) || this.conversionService.canConvert(String.class, propValueClass)) && ((propValue =
            this.getProperty(propName)) != null)) ? (propValueAssignable ? propValueClass.cast(propValue) : this.conversionService.convert(propValue,
            propValueClass)) : null);
    }

    @Override
    public boolean containsProperty(String propName) {
        return this.props.containsKey(propName);
    }

    @Nullable
    @Override
    protected String getPropertyAsRawString(String propName) {
        return this.props.getProperty(propName);
    }
}
