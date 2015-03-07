package gov.hhs.onc.phiz.crypto.utils;

import gov.hhs.onc.phiz.utils.PhizFunctionUtils;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.function.Function;
import javax.security.auth.x500.X500Principal;

public final class PhizCertificateUtils {
    private PhizCertificateUtils() {
    }

    public static BigInteger[] buildSerialNumbers(X509Certificate[] certs) {
        return PhizFunctionUtils.mapToArray(certs, X509Certificate::getSerialNumber, BigInteger[]::new);
    }

    public static String[] buildIssuerDnNames(X509Certificate[] certs) {
        return buildDnNames(certs, X509Certificate::getIssuerX500Principal);
    }

    public static X500Principal[] buildIssuerDns(X509Certificate[] certs) {
        return buildDns(certs, X509Certificate::getIssuerX500Principal);
    }

    public static String[] buildSubjectDnNames(X509Certificate[] certs) {
        return buildDnNames(certs, X509Certificate::getSubjectX500Principal);
    }

    public static X500Principal[] buildSubjectDns(X509Certificate[] certs) {
        return buildDns(certs, X509Certificate::getSubjectX500Principal);
    }

    public static String[] buildDnNames(X509Certificate[] certs, Function<X509Certificate, X500Principal> certDnMapper) {
        return PhizFunctionUtils.mapToArray(certs, certDnMapper.andThen(X500Principal::getName), String[]::new);
    }

    public static X500Principal[] buildDns(X509Certificate[] certs, Function<X509Certificate, X500Principal> certDnMapper) {
        return PhizFunctionUtils.mapToArray(certs, certDnMapper, X500Principal[]::new);
    }
}
