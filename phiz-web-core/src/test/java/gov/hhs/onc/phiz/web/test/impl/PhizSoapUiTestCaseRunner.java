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
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.github.sebhoss.warnings.CompilerWarnings;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;

@SuppressWarnings({ CompilerWarnings.DEPRECATION })
public class PhizSoapUiTestCaseRunner extends SoapUIProTestCaseRunner {
    private final static String SPRING_REF_PROP_NAME_PREFIX = PropertyExpansion.SCOPE_PREFIX + "Spring" + PropertyExpansion.PROPERTY_SEPARATOR;

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    private SSLParameters sslParams;
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

        if (httpsScheme.getSchemeSocketFactory() instanceof SoapUISSLSocketFactory) {
            httpSchemeReg.register(new org.apache.http.conn.scheme.Scheme(httpsScheme.getName(), httpsScheme.getDefaultPort(),
                new org.apache.http.conn.ssl.SSLSocketFactory(this.sslSocketFactory, this.sslParams.getProtocols(), this.sslParams.getCipherSuites(),
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));
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
        PropertyExpander
            .getDefaultExpander()
            .addResolver(
                (propExpContext, propName, globalOverride) -> {
                    if (!StringUtils.startsWith(propName, SPRING_REF_PROP_NAME_PREFIX)) {
                        return null;
                    }

                    String propNameResolving =
                        (PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX + (propName = StringUtils.removeStart(propName, SPRING_REF_PROP_NAME_PREFIX)) + PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_SUFFIX), propNameResolved =
                        this.beanFactory.resolveEmbeddedValue(propNameResolving);

                    return Objects.toString(
                        this.beanFactory.getBeanExpressionResolver().evaluate((!propNameResolved.equals(propNameResolving) ? propNameResolved : propName),
                            new BeanExpressionContext(this.beanFactory, null)), null);
                });

        super.initProjectProperties(project);
    }

    @Override
    protected void initGroovyLog() {
    }

    @Override
    protected SoapUICore createSoapUICore() {
        return new DefaultSoapUICore();
    }

    public SSLParameters getSslParams() {
        return this.sslParams;
    }

    public void setSslParams(SSLParameters sslParams) {
        this.sslParams = sslParams;
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
