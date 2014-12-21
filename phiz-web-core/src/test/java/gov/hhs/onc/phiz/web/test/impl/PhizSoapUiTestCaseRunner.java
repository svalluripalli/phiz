package gov.hhs.onc.phiz.web.test.impl;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUICore;
import com.eviware.soapui.SoapUIProTestCaseRunner;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.WsdlProjectProFactory;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.SoapUISSLSocketFactory;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.github.sebhoss.warnings.CompilerWarnings;
import gov.hhs.onc.phiz.context.PhizProperties;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.boot.bind.PropertySourceUtils;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

@SuppressWarnings({ CompilerWarnings.DEPRECATION })
public class PhizSoapUiTestCaseRunner extends SoapUIProTestCaseRunner {
    @Autowired
    private List<PropertySourcesPlaceholderConfigurer> propSourcesPlaceholderConfigurers;

    @Autowired
    private AbstractBeanFactory beanFactory;

    private SSLSocketFactory sslSocketFactory;
    private boolean projectInitialized;
    private CountDownLatch testCaseRunLatch;

    public PhizSoapUiTestCaseRunner() {
        super();
    }

    public boolean run(WsdlProjectPro project) throws Exception {
        ProjectFactoryRegistry.registrerProjectFactory(WsdlProjectProFactory.WSDL_TYPE, new WsdlProjectProFactory() {
            @Override
            public WsdlProjectPro createNew(String projectFile, String projectPass) {
                return project;
            }
        });

        this.projectInitialized = true;

        return this.run();
    }

    @Override
    public void runTestCase(WsdlTestCase testCase) {
        super.runTestCase(testCase);
    }

    @Override
    public void initProject(WsdlProject wsdlProject) {
        if (this.projectInitialized) {
            return;
        }

        org.apache.http.conn.scheme.SchemeRegistry httpSchemeReg = HttpClientSupport.getHttpClient().getConnectionManager().getSchemeRegistry();
        org.apache.http.conn.scheme.Scheme httpsScheme = httpSchemeReg.getScheme(RequestTransportRegistry.HTTPS);

        if (httpsScheme.getSocketFactory() instanceof SoapUISSLSocketFactory) {
            httpSchemeReg.register(new org.apache.http.conn.scheme.Scheme(httpsScheme.getName(), httpsScheme.getDefaultPort(),
                new org.apache.http.conn.ssl.SSLSocketFactory(sslSocketFactory, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));
        }

        super.initProject(wsdlProject);
    }

    @Override
    protected void runProject(WsdlProject project) {
        try {
            this.testCaseRunLatch.await();
        } catch (InterruptedException ignored) {
        }
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

    @Override
    protected SoapUICore createSoapUICore() {
        return new DefaultSoapUICore();
    }

    public SSLSocketFactory getSslSocketFactory() {
        return this.sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public CountDownLatch getTestCaseRunLatch() {
        return this.testCaseRunLatch;
    }

    public void setTestCaseRunLatch(CountDownLatch testCaseRunLatch) {
        this.testCaseRunLatch = testCaseRunLatch;
    }
}
