package gov.hhs.onc.phiz.web.test.soapui;

import javax.annotation.Nullable;

public class PhizSoapUiTestCaseException extends RuntimeException {
    private final static long serialVersionUID = 0L;

    public PhizSoapUiTestCaseException() {
        super();
    }

    public PhizSoapUiTestCaseException(@Nullable String msg) {
        super(msg);
    }

    public PhizSoapUiTestCaseException(@Nullable Throwable cause) {
        super(cause);
    }

    public PhizSoapUiTestCaseException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
