package gov.hhs.onc.phiz.beans.factory;

import org.springframework.beans.factory.InitializingBean;

public interface EmbeddedPlaceholderResolver extends InitializingBean {
    public String resolvePlaceholders(String str);

    public String resolvePlaceholders(String str, boolean asPropName);
}
