package gov.hhs.onc.phiz.beans.factory.impl;

import gov.hhs.onc.phiz.beans.factory.EmbeddedPlaceholderResolver;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.stereotype.Component;

@Component("embeddedPlaceholderResolverImpl")
public class EmbeddedPlaceholderResolverImpl implements EmbeddedPlaceholderResolver {
    @Autowired
    private ConfigurableBeanFactory beanFactory;

    private BeanExpressionResolver beanExprResolver;
    private BeanExpressionContext beanExprContext;

    @Override
    public String resolvePlaceholders(String str) {
        return this.resolvePlaceholders(str, false);
    }

    @Override
    public String resolvePlaceholders(String str, boolean asPropName) {
        if (asPropName) {
            String strResolving = PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX + str + PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_SUFFIX, strResolved =
                this.beanFactory.resolveEmbeddedValue(strResolving);

            if (!strResolved.equals(strResolving)) {
                str = strResolved;
            }
        } else {
            str = this.beanFactory.resolveEmbeddedValue(str);
        }

        return Objects.toString(this.beanExprResolver.evaluate(str, this.beanExprContext), null);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.beanExprResolver = this.beanFactory.getBeanExpressionResolver();
        this.beanExprContext = new BeanExpressionContext(this.beanFactory, null);
    }
}
