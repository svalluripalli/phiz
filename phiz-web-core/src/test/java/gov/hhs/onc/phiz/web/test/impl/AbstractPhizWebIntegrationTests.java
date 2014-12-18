package gov.hhs.onc.phiz.web.test.impl;

import gov.hhs.onc.phiz.context.impl.PhizApplication;
import gov.hhs.onc.phiz.test.impl.AbstractPhizIntegrationTests;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = { PhizApplication.class }, loader = PhizWebApplicationTestContextLoader.class)
@Test(groups = { "phiz.test.web.all", "phiz.test.it.web.all" })
public abstract class AbstractPhizWebIntegrationTests extends AbstractPhizIntegrationTests {
}
