package gov.hhs.onc.phiz.web.test.impl;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUICore;
import com.eviware.soapui.SoapUIProTestCaseRunner;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.WsdlProjectProFactory;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.WsdlTestSuitePro;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.SoapUISSLSocketFactory;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestSuiteRunContext;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestSuiteRunner;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.github.sebhoss.warnings.CompilerWarnings;
import gov.hhs.onc.phiz.beans.factory.EmbeddedPlaceholderResolver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings({ CompilerWarnings.DEPRECATION })
public class PhizSoapUiTestCaseRunner extends SoapUIProTestCaseRunner {
    private final static String SPRING_REF_PROP_NAME_PREFIX = PropertyExpansion.SCOPE_PREFIX + "Spring" + PropertyExpansion.PROPERTY_SEPARATOR;

    @Autowired
    private EmbeddedPlaceholderResolver embeddedPlaceholderResolver;

    private SSLParameters sslParams;
    private SSLSocketFactory sslSocketFactory;
    private boolean projectInitialized;
    private CountDownLatch projectRunLatch;
    private WsdlTestSuite testSuite;
    private CountDownLatch testSuiteRunLatch;
    private FutureTask<Void> testSuiteRunTask;

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
        WsdlTestSuite testCaseTestSuite = testCase.getTestSuite();

        if ((this.testSuite == null) || !testCaseTestSuite.getName().equals(this.testSuite.getName())) {
            if (this.testSuite != null) {
                this.testSuiteRunLatch.countDown();

                try {
                    this.testSuiteRunTask.get();
                } catch (ExecutionException | InterruptedException ignored) {
                }
            }

            this.testSuite = testCaseTestSuite;
            this.testSuiteRunLatch = new CountDownLatch(1);

            this.testSuiteRunTask = new FutureTask<>(() -> {
                this.runSuite(new WsdlTestSuitePro(((WsdlProject) testCase.getProject()), this.testSuite.getConfig()) {
                    @Override
                    public WsdlTestSuiteRunner run(StringToObjectMap context, boolean async) {
                        WsdlTestSuiteRunner testSuiteRunner = new WsdlTestSuiteRunner(this, context) {
                            @Override
                            public void internalRun(WsdlTestSuiteRunContext runContext) throws Exception {
                                try {
                                    PhizSoapUiTestCaseRunner.this.testSuiteRunLatch.await();
                                } catch (InterruptedException ignored) {
                                }
                            }
                        };

                        testSuiteRunner.start(async);

                        return testSuiteRunner;
                    }
                });

                return null;
            });

            Thread testSuiteRunThread = new Thread(this.testSuiteRunTask);
            testSuiteRunThread.setDaemon(true);
            testSuiteRunThread.start();
        }

        if (testCase.getLoadTestCount() > 0) {
            testCase.getLoadTestList().stream().forEach(LoadTest::run);
        } else {
            super.runTestCase(testCase);
        }
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
            this.projectRunLatch.await();
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    protected void initProjectProperties(WsdlProject project) {
        PropertyExpander.getDefaultExpander().addResolver(
            (propExpContext, propName, globalOverride) -> {
                return (StringUtils.startsWith(propName, SPRING_REF_PROP_NAME_PREFIX) ? this.embeddedPlaceholderResolver.resolvePlaceholders(
                    StringUtils.removeStart(propName, SPRING_REF_PROP_NAME_PREFIX), true) : null);
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

    public CountDownLatch getProjectRunLatch() {
        return this.projectRunLatch;
    }

    public void setProjectRunLatch(CountDownLatch testCaseRunLatch) {
        this.projectRunLatch = testCaseRunLatch;
    }
}
