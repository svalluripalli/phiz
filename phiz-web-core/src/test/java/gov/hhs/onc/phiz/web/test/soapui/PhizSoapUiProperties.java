package gov.hhs.onc.phiz.web.test.soapui;

import gov.hhs.onc.phiz.context.PhizProperties;

public final class PhizSoapUiProperties {
    public final static String PREFIX = PhizProperties.PREFIX + "test.soapui.";
    public final static String EXCEPTION_PREFIX = PREFIX + "exception.";
    public final static String SSL_PREFIX = PREFIX + "ssl.";

    public final static String EXCEPTION_CLASSES_NAME = EXCEPTION_PREFIX + "classes";

    public final static String SSL_PARAMS_NAME = SSL_PREFIX + "params";
    public final static String BAD_CIPHER_SUITES_SSL_PARAMS_VALUE = "bad.cipher.suites";
    public final static String BAD_PROTOCOL_VERSIONS_SSL_PARAMS_VALUE = "bad.protocol.versions";

    public final static String SSL_SOCKET_FACTORY_NAME = SSL_PREFIX + "socket.factory";
    public final static String INVALID_KEY_SIZE_SSL_SOCKET_FACTORY_VALUE = "key.size";
    public final static String INVALID_SIG_ALG_SSL_SOCKET_FACTORY_VALUE = "sig.alg";
    public final static String REVOKED_SSL_SOCKET_FACTORY_VALUE = "revoked";
    public final static String UNTRUSTED_SSL_SOCKET_FACTORY_VALUE = "untrusted";

    private PhizSoapUiProperties() {
    }
}
