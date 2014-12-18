package gov.hhs.onc.phiz.tools.test.impl;

import gov.hhs.onc.phiz.tools.PhizTool;
import gov.hhs.onc.phiz.tools.PhizToolOptions;
import org.testng.annotations.Test;

@Test(groups = { "phiz.test.it.tools.tool.all" })
public abstract class AbstractPhizToolIntegrationTests<T extends PhizToolOptions, U extends PhizTool<T>> extends AbstractPhizToolsIntegrationTests {
    protected Class<T> toolOptsClass;
    protected Class<U> toolClass;
    protected T toolOpts;
    protected U tool;

    protected AbstractPhizToolIntegrationTests(Class<T> toolOptsClass, Class<U> toolClass) {
        this.toolOptsClass = toolOptsClass;
        this.toolClass = toolClass;
    }

    public void initializeTool() throws Exception {
        this.toolOpts = this.applicationContext.getBean(this.toolOptsClass);
        this.tool = this.applicationContext.getBean(this.toolClass);
    }
}
