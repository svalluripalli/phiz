package gov.hhs.onc.phiz.crypto;

public final class PhizTlsVersions {
    private final static String SSL_PREFIX = "SSL";
    private final static String TLS_PREFIX = "TLS";

    private final static String NAME_DELIM = "v";
    private final static String VERSION_DELIM = ".";

    public final static int SSL_2_VERSION_MAJOR = 2;
    public final static int SSL_2_VERSION_MINOR = 0;
    public final static String SSL_2_NAME = SSL_PREFIX + NAME_DELIM + SSL_2_VERSION_MAJOR;

    public final static int SSL_3_VERSION_MAJOR = 3;
    public final static int SSL_3_VERSION_MINOR = 0;
    public final static String SSL_3_NAME = SSL_PREFIX + NAME_DELIM + SSL_3_VERSION_MAJOR;

    public final static int TLS_1_VERSION_MAJOR = 1;
    public final static int TLS_1_VERSION_MINOR = 0;
    public final static String TLS_1_NAME = TLS_PREFIX + NAME_DELIM + TLS_1_VERSION_MAJOR;

    public final static int TLS_1_1_VERSION_MAJOR = TLS_1_VERSION_MAJOR;
    public final static int TLS_1_1_VERSION_MINOR = 1;
    public final static String TLS_1_1_NAME = TLS_1_NAME + VERSION_DELIM + TLS_1_1_VERSION_MINOR;

    public final static int TLS_1_2_VERSION_MAJOR = TLS_1_VERSION_MAJOR;
    public final static int TLS_1_2_VERSION_MINOR = 2;
    public final static String TLS_1_2_NAME = TLS_1_NAME + VERSION_DELIM + TLS_1_2_VERSION_MINOR;

    private PhizTlsVersions() {
    }
}
