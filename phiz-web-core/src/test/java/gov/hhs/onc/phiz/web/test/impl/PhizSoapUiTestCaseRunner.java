package gov.hhs.onc.phiz.web.test.impl;

import com.eviware.soapui.SoapUIProTestCaseRunner;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import gov.hhs.onc.phiz.context.PhizProperties;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.boot.bind.PropertySourceUtils;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

public class PhizSoapUiTestCaseRunner extends SoapUIProTestCaseRunner {
    @Autowired
    private List<PropertySourcesPlaceholderConfigurer> propSourcesPlaceholderConfigurers;

    @Autowired
    private AbstractBeanFactory beanFactory;

    public PhizSoapUiTestCaseRunner() {
        super();
    }

    public PhizSoapUiTestCaseRunner(String title) {
        super(title);
    }

    @Override
    protected void initProjectProperties(WsdlProject project) {
        super.initProjectProperties(project);

        BeanExpressionResolver beanExprResolver = this.beanFactory.getBeanExpressionResolver();
        BeanExpressionContext beanExprContext = new BeanExpressionContext(this.beanFactory, null);
        MutablePropertySources propSources = new MutablePropertySources();
        PropertySourcesPropertyResolver propSourcesPropResolver = new PropertySourcesPropertyResolver(propSources);

        this.propSourcesPlaceholderConfigurers.forEach((propSourcesPlaceholderConfigurer) -> propSources.addLast(new MapPropertySource(
            (PropertySourcesPlaceholderConfigurer.LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME + UUID.randomUUID()), PropertySourceUtils.getSubProperties(
                propSourcesPlaceholderConfigurer.getAppliedPropertySources(), StringUtils.EMPTY))));

        PropertySourceUtils.getSubProperties(propSources, PhizProperties.PREFIX).forEach(
            (propName, propValue) -> {
                if (!project.hasProperty((propName = (PhizProperties.PREFIX + propName)))) {
                    project.setPropertyValue(propName, Objects.toString(
                        beanExprResolver.evaluate(propSourcesPropResolver.resolveRequiredPlaceholders(Objects.toString(propValue, null)), beanExprContext),
                        null));
                }
            });
    }

    @Override
    protected void initGroovyLog() {
    }
}
