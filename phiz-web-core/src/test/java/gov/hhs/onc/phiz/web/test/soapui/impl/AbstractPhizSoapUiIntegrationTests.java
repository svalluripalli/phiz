package gov.hhs.onc.phiz.web.test.soapui.impl;

import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.WsdlTestCasePro;
import gov.hhs.onc.phiz.web.test.soapui.impl.AbstractPhizSoapUiIntegrationTests.PhizSoapUiTestCaseMethodInterceptor;
import gov.hhs.onc.phiz.web.test.impl.AbstractPhizWebIntegrationTests;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({ PhizSoapUiTestCaseMethodInterceptor.class })
@Test(groups = { "phiz.test.it.web.soapui.all" })
public abstract class AbstractPhizSoapUiIntegrationTests extends AbstractPhizWebIntegrationTests {
    public static class PhizSoapUiTestCaseMethodInterceptor implements IMethodInterceptor {
        @Override
        public List<IMethodInstance> intercept(List<IMethodInstance> methodInstances, ITestContext testContext) {
            methodInstances.sort(Comparator.comparingInt(methodInstance -> ((AbstractPhizSoapUiIntegrationTests) methodInstance.getInstance()).testCaseOrder));

            return methodInstances;
        }
    }

    protected abstract static class AbstractPhizSoapUiTestCaseIntegrationTestsFactory<T extends AbstractPhizSoapUiIntegrationTests> {
        protected Class<T> testCaseTestsClass;
        protected Supplier<T> testCaseTestsClassBuilder;
        protected IntFunction<T[]> testCaseTestsArrayBuilder;
        protected Method testCaseTestsTestMethod;

        protected AbstractPhizSoapUiTestCaseIntegrationTestsFactory(Class<T> testCaseTestsClass, Supplier<T> testCaseTestsClassBuilder,
            IntFunction<T[]> testCaseTestsArrayBuilder, String testCaseTestsTestMethodName) throws Exception {
            this.testCaseTestsClass = testCaseTestsClass;
            this.testCaseTestsClassBuilder = testCaseTestsClassBuilder;
            this.testCaseTestsArrayBuilder = testCaseTestsArrayBuilder;
            this.testCaseTestsTestMethod = this.testCaseTestsClass.getMethod(testCaseTestsTestMethodName);
        }

        @Factory
        public Object[] getTestCaseTests(ITestContext testContext) throws Exception {
            String[] testCaseTestsTestGroups =
                Stream
                    .concat(Stream.of(AnnotationUtils.getAnnotations(this.testCaseTestsClass)),
                        Stream.of(AnnotationUtils.getAnnotations(this.testCaseTestsTestMethod))).filter(anno -> (anno instanceof Test))
                    .flatMap(anno -> Stream.of(((Test) anno).groups())).distinct().toArray(String[]::new);

            if (Stream.of(testCaseTestsTestGroups).noneMatch(
                testCaseTestsTestGroup -> Stream.of(testContext.getIncludedGroups()).anyMatch(testCaseTestsTestGroup::matches))
                || Stream.of(testCaseTestsTestGroups).anyMatch(
                    testCaseTestsTestGroup -> Stream.of(testContext.getExcludedGroups()).anyMatch(testCaseTestsTestGroup::matches))) {
                return ArrayUtils.EMPTY_OBJECT_ARRAY;
            }

            PhizSoapUiTestCaseRunner testCaseRunner = new TestContextManager(this.testCaseTestsClass) {
                public TestContext getTestContextExternal() {
                    return this.getTestContext();
                }
            }.getTestContextExternal().getApplicationContext().getBean(PhizSoapUiTestCaseRunner.class);

            WsdlProjectPro project = new WsdlProjectPro(testCaseRunner.getProjectFile());
            testCaseRunner.initProject(project);

            List<WsdlTestCasePro> testCases =
                project
                    .getTestSuiteList()
                    .stream()
                    .filter(testSuite -> !testSuite.isDisabled())
                    .flatMap(
                        testSuite -> testSuite.getTestCaseList().stream().filter(testCase -> !testCase.isDisabled())
                            .map(testCase -> ((WsdlTestCasePro) testCase))).collect(Collectors.toList());

            CountDownLatch projectRunLatch = new CountDownLatch(testCases.size());
            testCaseRunner.setProjectRunLatch(projectRunLatch);

            FutureTask<Void> projectRunTask = new FutureTask<>(() -> {
                testCaseRunner.run(project);

                return null;
            });

            Thread projectRunThread = new Thread(projectRunTask);
            projectRunThread.setDaemon(true);
            projectRunThread.start();

            T[] testCaseTestsInstances = this.testCaseTestsArrayBuilder.apply(testCases.size());

            for (int a = 0; a < testCaseTestsInstances.length; a++) {
                testCaseTestsInstances[a] = this.testCaseTestsClassBuilder.get();
                testCaseTestsInstances[a].testCaseRunner = testCaseRunner;
                testCaseTestsInstances[a].projectRunTask = projectRunTask;
                testCaseTestsInstances[a].projectRunLatch = projectRunLatch;
                testCaseTestsInstances[a].testCase = testCases.get(a);
                testCaseTestsInstances[a].testCaseOrder = a;
            }

            return testCaseTestsInstances;
        }
    }

    protected PhizSoapUiTestCaseRunner testCaseRunner;
    protected FutureTask<Void> projectRunTask;
    protected CountDownLatch projectRunLatch;
    protected WsdlTestCasePro testCase;
    protected int testCaseOrder;

    public void testTestCase() throws Exception {
        try {
            this.testCaseRunner.runTestCase(this.testCase);
        } finally {
            this.projectRunLatch.countDown();

            if (this.projectRunLatch.getCount() == 0) {
                try {
                    this.projectRunTask.get();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
