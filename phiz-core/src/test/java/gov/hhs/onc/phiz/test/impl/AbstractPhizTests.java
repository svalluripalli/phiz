package gov.hhs.onc.phiz.test.impl;

import gov.hhs.onc.phiz.context.impl.PhizApplicationConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.testng.annotations.Test;

@ContextConfiguration(classes = { PhizApplicationConfiguration.class }, loader = PhizApplicationTestContextLoader.class)
@Test(groups = { "phiz.test.all" })
@TestExecutionListeners(
    listeners = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class },
    inheritListeners = false)
public abstract class AbstractPhizTests extends AbstractTestNGSpringContextTests {
}
