package gov.hhs.onc.phiz.crypto;

import org.springframework.util.MimeType;

public final class PhizCryptoContentTypes {
    public final static String OCSP_REQ_TYPE = "application";
    public final static String OCSP_REQ_SUBTYPE = "ocsp-request";
    public final static MimeType OCSP_REQ = new MimeType(OCSP_REQ_TYPE, OCSP_REQ_SUBTYPE);

    public final static String OCSP_RESP_TYPE = "application";
    public final static String OCSP_RESP_SUBTYPE = "ocsp-response";
    public final static MimeType OCSP_RESP = new MimeType(OCSP_RESP_TYPE, OCSP_RESP_SUBTYPE);

    private PhizCryptoContentTypes() {
    }
}
