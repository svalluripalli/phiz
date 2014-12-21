package gov.hhs.onc.phiz.web.test.impl;

import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.WsdlTestCasePro;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.testng.ITestContext;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@Test(groups = { "phiz.test.it.web.soapui.all" })
public abstract class AbstractPhizSoapUiIntegrationTests extends AbstractPhizWebIntegrationTests {
    protected abstract static class AbstractPhizSoapUiTestCaseIntegrationTestsFactory<T extends AbstractPhizSoapUiIntegrationTests> {
        protected Class<T> testCaseTestsClass;
        protected Supplier<T> testCaseTestsClassBuilder;
        protected Method testCaseTestsTestMethod;

        protected AbstractPhizSoapUiTestCaseIntegrationTestsFactory(Class<T> testCaseTestsClass, Supplier<T> testCaseTestsClassBuilder,
            String testCaseTestsTestMethodName) throws Exception {
            this.testCaseTestsClass = testCaseTestsClass;
            this.testCaseTestsClassBuilder = testCaseTestsClassBuilder;
            this.testCaseTestsTestMethod = this.testCaseTestsClass.getMethod(testCaseTestsTestMethodName);
        }

        @Factory
        public Object[] getTestCaseTests(ITestContext testContext) throws Exception {
            String[] testCaseTestsTestGroups =
                Stream
                    .concat(Stream.of(AnnotationUtils.getAnnotations(this.testCaseTestsClass)),
                        Stream.of(AnnotationUtils.getAnnotations(this.testCaseTestsTestMethod))).filter((anno) -> (anno instanceof Test))
                    .flatMap((anno) -> Stream.of(((Test) anno).groups())).distinct().toArray(String[]::new);

            if (Stream.of(testCaseTestsTestGroups).noneMatch(
                (testCaseTestsTestGroup) -> Stream.of(testContext.getIncludedGroups()).anyMatch(testCaseTestsTestGroup::matches))
                || Stream.of(testCaseTestsTestGroups).anyMatch(
                    (testCaseTestsTestGroup) -> Stream.of(testContext.getExcludedGroups()).anyMatch(testCaseTestsTestGroup::matches))) {
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
                    .flatMap(
                        (testSuite) -> testSuite.getTestCaseList().stream().filter((testCase) -> !testCase.isDisabled())
                            .map((testCase) -> ((WsdlTestCasePro) testCase))).collect(Collectors.toList());

            CountDownLatch testCaseRunLatch = new CountDownLatch(testCases.size());
            testCaseRunner.setTestCaseRunLatch(testCaseRunLatch);

            FutureTask<Void> testCaseRunTask = new FutureTask<>(() -> {
                testCaseRunner.run(project);

                return null;
            });

            Thread testCaseRunThread = new Thread(testCaseRunTask);
            testCaseRunThread.setDaemon(true);
            testCaseRunThread.start();

            return testCases.stream().map((testCase) -> {
                T testCaseTestsInstance = this.testCaseTestsClassBuilder.get();
                testCaseTestsInstance.testCaseRunner = testCaseRunner;
                testCaseTestsInstance.testCaseRunTask = testCaseRunTask;
                testCaseTestsInstance.testCaseRunLatch = testCaseRunLatch;
                testCaseTestsInstance.testCase = testCase;

                return testCaseTestsInstance;
            }).toArray();
        }
    }

    protected PhizSoapUiTestCaseRunner testCaseRunner;
    protected FutureTask<Void> testCaseRunTask;
    protected CountDownLatch testCaseRunLatch;
    protected WsdlTestCasePro testCase;

    public void testTestCase() throws Exception {
        try {
            this.testCaseRunner.runTestCase(this.testCase);
        } finally {
            this.testCaseRunLatch.countDown();

            if (this.testCaseRunLatch.getCount() == 0) {
                try {
                    this.testCaseRunTask.get();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
