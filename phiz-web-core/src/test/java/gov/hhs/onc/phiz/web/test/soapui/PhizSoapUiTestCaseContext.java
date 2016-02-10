package gov.hhs.onc.phiz.web.test.soapui;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import javax.annotation.Nullable;

public interface PhizSoapUiTestCaseContext {
    public boolean isTestCaseExceptionExpected();

    public WsdlTestCase getTestCase();

    @Nullable
    public Class<?>[] getTestCaseExceptionClasses();

    public WsdlTestRunContext getTestCaseRunContext();

    public void setTestCaseRunContext(WsdlTestRunContext testCaseRunContext);
}
