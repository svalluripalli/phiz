package gov.hhs.onc.phiz.web.test.soapui.impl;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUICore;
import com.eviware.soapui.SoapUIProTestCaseRunner;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.WsdlProjectProFactory;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlTestCasePro;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.WsdlTestSuitePro;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestSuiteRunContext;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestSuiteRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.github.sebhoss.warnings.CompilerWarnings;
import gov.hhs.onc.phiz.beans.factory.EmbeddedPlaceholderResolver;
import gov.hhs.onc.phiz.web.test.soapui.PhizSoapUiProperties;
import gov.hhs.onc.phiz.web.test.soapui.PhizSoapUiTestCaseContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings({ CompilerWarnings.DEPRECATION })
public class PhizSoapUiTestCaseRunner extends SoapUIProTestCaseRunner implements SubmitListener {
    private final static String SPRING_REF_PROP_NAME_PREFIX = PropertyExpansion.SCOPE_PREFIX + "Spring" + PropertyExpansion.PROPERTY_SEPARATOR;

    @Autowired
    private EmbeddedPlaceholderResolver embeddedPlaceholderResolver;

    private Map<String, SSLParameters> sslParamMap;
    private Map<String, SSLSocketFactory> sslSocketFactoryMap;
    private boolean projectInitialized;
    private CountDownLatch projectRunLatch;
    private WsdlTestSuite testSuite;
    private CountDownLatch testSuiteRunLatch;
    private FutureTask<Void> testSuiteRunTask;
    private Map<String, PhizSoapUiTestCaseContext> testCaseContextMap;
    private Map<String, WsdlSubmit<WsdlRequest>> testCaseSubmitMap;
    private Map<String, Exception> testCaseExceptionMap;

    public PhizSoapUiTestCaseRunner() {
        super();
    }

    @Override
    public void afterRun(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext) {

        super.afterRun(testCaseRunner, testCaseRunContext);
    }

    @Override
    public void afterStep(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext, TestStepResult testStepResult) {
        String testCaseName;
        PhizSoapUiTestCaseContext testCaseContext;

        if ((testStepResult.getStatus() == TestStepStatus.FAILED)
            && (testCaseContext = this.testCaseContextMap.get((testCaseName = testCaseRunContext.getTestCase().getName()))).isTestCaseExceptionExpected()) {
            Exception testStepException = ((Exception) testStepResult.getError());

            if (testStepException == null) {
                testStepException = this.testCaseSubmitMap.get(testCaseName).getError();
            }

            if (testStepException != null) {
                final Class<? extends Exception> testStepExceptionClass = testStepException.getClass();

                // noinspection ConstantConditions
                if (Stream.of(testCaseContext.getTestCaseExceptionClasses()).anyMatch(
                    testCaseExceptionClass -> testCaseExceptionClass.isAssignableFrom(testStepExceptionClass))) {
                    ((WsdlTestStepResult) testStepResult).setStatus(TestStepStatus.OK);

                    this.testCaseExceptionMap.put(testCaseName, testStepException);
                }
            }
        }

        super.afterStep(testCaseRunner, testCaseRunContext, testStepResult);
    }

    @Override
    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    public void afterSubmit(Submit submit, SubmitContext submitContext) {
        this.testCaseSubmitMap.put(((WsdlTestRequest) submit.getRequest()).getTestCase().getName(), ((WsdlSubmit<WsdlRequest>) submit));
    }

    @Override
    public boolean beforeSubmit(Submit submit, SubmitContext submitContext) {
        return true;
    }

    @Override
    public void beforeRun(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext) {
        this.testCaseContextMap.get(testCaseRunContext.getTestCase().getName()).setTestCaseRunContext(((WsdlTestRunContext) testCaseRunContext));

        super.beforeRun(testCaseRunner, testCaseRunContext);
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

    public void runTestCase(PhizSoapUiTestCaseContext testCaseContext) throws Exception {
        WsdlTestCasePro testCase = testCaseContext.getTestCase();
        WsdlTestSuite testSuite = testCase.getTestSuite();

        if ((this.testSuite == null) || !testSuite.getName().equals(this.testSuite.getName())) {
            if (this.testSuite != null) {
                this.testSuiteRunLatch.countDown();

                try {
                    this.testSuiteRunTask.get();
                } catch (ExecutionException | InterruptedException ignored) {
                }
            }

            this.testSuite = testSuite;
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

        this.initializeSslScheme(this.sslParamMap.get(testCase.getPropertyValue(PhizSoapUiProperties.SSL_PARAMS_NAME)),
            this.sslSocketFactoryMap.get(testCase.getPropertyValue(PhizSoapUiProperties.SSL_SOCKET_FACTORY_NAME)));

        if (testCase.getLoadTestCount() > 0) {
            testCase.getLoadTestList().stream().forEach(LoadTest::run);
        } else {
            String testCaseName = testCase.getName();

            this.testCaseContextMap.put(testCaseName, testCaseContext);

            this.runTestCase(testCase);

            if (this.testCaseExceptionMap.containsKey(testCaseName)) {
                throw this.testCaseExceptionMap.get(testCaseName);
            }
        }

        this.initializeSslScheme(this.sslParamMap.get(null), this.sslSocketFactoryMap.get(null));
    }

    @Override
    public void initProject(WsdlProject project) {
        if (this.projectInitialized) {
            return;
        }

        SoapUI.getListenerRegistry().addSingletonListener(SubmitListener.class, this);

        HttpClientSupport.getHttpClient().setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));

        this.initializeSslScheme(this.sslParamMap.get(null), this.sslSocketFactoryMap.get(null));

        // noinspection ConstantConditions
        int numTestCases =
            IntStream.of(ArrayUtils.toPrimitive(project.getTestSuiteList().stream().map(TestSuite::getTestCaseCount).toArray(Integer[]::new))).sum();

        this.testCaseContextMap = new ConcurrentHashMap<>(numTestCases);
        this.testCaseSubmitMap = new ConcurrentHashMap<>(numTestCases);
        this.testCaseExceptionMap = new ConcurrentHashMap<>(numTestCases);

        super.initProject(project);
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

    private void initializeSslScheme(SSLParameters sslParams, SSLSocketFactory sslSocketFactory) {
        org.apache.http.conn.scheme.SchemeRegistry httpSchemeReg = HttpClientSupport.getHttpClient().getConnectionManager().getSchemeRegistry();
        org.apache.http.conn.scheme.Scheme httpsScheme = httpSchemeReg.getScheme(RequestTransportRegistry.HTTPS);

        httpSchemeReg.register(new org.apache.http.conn.scheme.Scheme(httpsScheme.getName(), httpsScheme.getDefaultPort(),
            new org.apache.http.conn.ssl.SSLSocketFactory(sslSocketFactory, sslParams.getProtocols(), sslParams.getCipherSuites(),
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));
    }

    public CountDownLatch getProjectRunLatch() {
        return this.projectRunLatch;
    }

    public void setProjectRunLatch(CountDownLatch testCaseRunLatch) {
        this.projectRunLatch = testCaseRunLatch;
    }

    public Map<String, SSLParameters> getSslParameterMap() {
        return this.sslParamMap;
    }

    public void setSslParameterMap(Map<String, SSLParameters> sslParamMap) {
        this.sslParamMap = sslParamMap;
    }

    public Map<String, SSLSocketFactory> getSslSocketFactoryMap() {
        return this.sslSocketFactoryMap;
    }

    public void setSslSocketFactoryMap(Map<String, SSLSocketFactory> sslSocketFactoryMap) {
        this.sslSocketFactoryMap = sslSocketFactoryMap;
    }
}
