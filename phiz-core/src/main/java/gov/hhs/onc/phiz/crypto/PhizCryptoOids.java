package gov.hhs.onc.phiz.crypto;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;

public final class PhizCryptoOids {
    public final static ASN1ObjectIdentifier ID_PKIX_OCSP_PREF_SIG_ALGS = OCSPObjectIdentifiers.id_pkix_ocsp.branch(Integer.toString(8));

    public final static ASN1ObjectIdentifier ID_PKIX_OCSP_EXTENDED_REVOKE = OCSPObjectIdentifiers.id_pkix_ocsp.branch(Integer.toString(9));

    private PhizCryptoOids() {
    }
}
