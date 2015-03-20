package gov.hhs.onc.phiz.web.test.soapui;

import com.eviware.soapui.impl.wsdl.WsdlTestCasePro;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import javax.annotation.Nullable;

public interface PhizSoapUiTestCaseContext {
    public boolean isTestCaseExceptionExpected();

    public WsdlTestCasePro getTestCase();

    @Nullable
    public Class<?>[] getTestCaseExceptionClasses();

    public WsdlTestRunContext getTestCaseRunContext();

    public void setTestCaseRunContext(WsdlTestRunContext testCaseRunContext);
}
