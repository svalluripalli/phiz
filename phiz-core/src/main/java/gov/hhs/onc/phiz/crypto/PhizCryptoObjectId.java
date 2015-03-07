package gov.hhs.onc.phiz.crypto;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface PhizCryptoObjectId extends PhizCryptoId {
    public ASN1ObjectIdentifier getOid();
}
