package gov.hhs.onc.phiz.crypto.ssl.revocation;

import gov.hhs.onc.phiz.crypto.PhizCryptoTagId;
import gov.hhs.onc.phiz.utils.PhizStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.ocsp.OCSPResp;

public enum OcspResponseStatusType implements PhizCryptoTagId {
    SUCCESSFUL(OCSPResp.SUCCESSFUL), MALFORMED_REQUEST(OCSPResp.MALFORMED_REQUEST), INTERNAL_ERROR(OCSPResp.INTERNAL_ERROR), TRY_LATER(OCSPResp.TRY_LATER),
    SIG_REQUIRED(OCSPResp.SIG_REQUIRED), UNAUTHORIZED(OCSPResp.UNAUTHORIZED);

    private final int tag;
    private final String id;

    private OcspResponseStatusType(int tag) {
        this.tag = tag;
        this.id = PhizStringUtils.joinCamelCase(StringUtils.split(this.name(), PhizStringUtils.UNDERSCORE));
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getTag() {
        return this.tag;
    }
}
