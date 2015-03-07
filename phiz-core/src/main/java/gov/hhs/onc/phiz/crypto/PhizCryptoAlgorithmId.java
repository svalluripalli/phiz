package gov.hhs.onc.phiz.crypto;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface PhizCryptoAlgorithmId extends PhizCryptoObjectId {
    public AlgorithmIdentifier getAlgorithmId();
}
