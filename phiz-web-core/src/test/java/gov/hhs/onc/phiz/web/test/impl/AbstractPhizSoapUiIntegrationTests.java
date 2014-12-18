package gov.hhs.onc.phiz.web.test.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

@Test(groups = { "phiz.test.it.web.soapui.all" })
public abstract class AbstractPhizSoapUiIntegrationTests extends AbstractPhizWebIntegrationTests {
    @Autowired
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    protected PhizSoapUiTestCaseRunner testCaseRunner;
    
    public void testSoapUiProject() throws Exception {
        this.testCaseRunner.run();
    }
}
