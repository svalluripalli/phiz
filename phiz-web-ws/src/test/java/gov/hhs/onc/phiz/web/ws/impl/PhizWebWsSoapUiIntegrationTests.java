package gov.hhs.onc.phiz.web.ws.impl;

import gov.hhs.onc.phiz.web.test.soapui.impl.AbstractPhizSoapUiIntegrationTests;
import org.testng.annotations.Test;

public class PhizWebWsSoapUiIntegrationTests extends AbstractPhizSoapUiIntegrationTests {
    public static class PhizWebWsSoapUiTestCaseIntegrationTestsFactory extends
        AbstractPhizSoapUiTestCaseIntegrationTestsFactory<PhizWebWsSoapUiIntegrationTests> {
        public PhizWebWsSoapUiTestCaseIntegrationTestsFactory() throws Exception {
            super(PhizWebWsSoapUiIntegrationTests.class, PhizWebWsSoapUiIntegrationTests::new, PhizWebWsSoapUiIntegrationTests[]::new, "testTestCase");
        }
    }

    @Override
    @Test(groups = { "phiz.test.it.web.ws.all", "phiz.test.it.web.ws.soapui" })
    public void testTestCase() throws Exception {
        super.testTestCase();
    }
}
