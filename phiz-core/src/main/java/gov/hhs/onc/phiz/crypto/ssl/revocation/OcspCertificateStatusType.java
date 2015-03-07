package gov.hhs.onc.phiz.crypto.ssl.revocation;

import gov.hhs.onc.phiz.crypto.PhizCryptoTagId;
import gov.hhs.onc.phiz.crypto.PhizCryptoTypeId;
import gov.hhs.onc.phiz.utils.PhizStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.cert.ocsp.UnknownStatus;

public enum OcspCertificateStatusType implements PhizCryptoTagId, PhizCryptoTypeId {
    GOOD(CertificateStatus.class), REVOKED(RevokedStatus.class), UNKNOWN(UnknownStatus.class);

    private final int tag;
    private final String id;
    private final Class<?> type;

    private OcspCertificateStatusType(Class<?> type) {
        this.tag = this.ordinal();
        this.id = PhizStringUtils.joinCamelCase(StringUtils.split(this.name(), PhizStringUtils.UNDERSCORE));
        this.type = type;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getOrder() {
        return (this.tag * -1);
    }

    @Override
    public int getTag() {
        return this.tag;
    }

    @Override
    public Class<?> getType() {
        return this.type;
    }
}
