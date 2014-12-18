package gov.hhs.onc.phiz.tools.impl;

import gov.hhs.onc.phiz.context.PhizProperties;
import gov.hhs.onc.phiz.tools.test.impl.AbstractPhizToolIntegrationTests;
import org.springframework.test.context.TestPropertySource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = { "phiz.test.it.tools.tool.tls.gen.creds" })
@TestPropertySource(properties = { (PhizProperties.APP_NAME_NAME + "=phiz-tools-tls-gen-creds") })
public class TlsGenerateCredentialsToolIntegrationTests extends AbstractPhizToolIntegrationTests<TlsGenerateCredentialsToolOptions, TlsGenerateCredentialsTool> {
    public TlsGenerateCredentialsToolIntegrationTests() {
        super(TlsGenerateCredentialsToolOptions.class, TlsGenerateCredentialsTool.class);
    }

    @Test
    public void testToolInitialized() throws Exception {
        Assert.assertNotNull(this.toolOpts, String.format("Tool options bean was not initialized."));
        Assert.assertNotNull(this.tool, String.format("Tool bean was not initialized."));
    }

    @BeforeClass(groups = { "phiz.test.it.tools.tool.tls.gen.creds" })
    @Override
    public void initializeTool() throws Exception {
        super.initializeTool();
    }
}
