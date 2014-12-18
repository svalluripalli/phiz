package gov.hhs.onc.phiz.web.ws.impl;

import gov.hhs.onc.phiz.web.test.impl.AbstractPhizSoapUiIntegrationTests;
import org.testng.annotations.Test;

@Test(groups = { "phiz.test.it.web.ws.all", "phiz.test.it.web.ws.soapui" })
public class PhizWebWsSoapUiIntegrationTests extends AbstractPhizSoapUiIntegrationTests {
    @Override
    @Test
    public void testSoapUiProject() throws Exception {
        super.testSoapUiProject();
    }
}
