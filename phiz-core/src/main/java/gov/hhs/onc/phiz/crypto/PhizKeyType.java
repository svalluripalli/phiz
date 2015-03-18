package gov.hhs.onc.phiz.crypto;

import java.security.interfaces.DSAKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import javax.crypto.interfaces.DHKey;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;

public enum PhizKeyType implements PhizCryptoObjectId, PhizCryptoTypeId {
    DH("DH", PKCSObjectIdentifiers.dhKeyAgreement, DHKey.class), DSA("DSA", X9ObjectIdentifiers.id_dsa, DSAKey.class), EC("EC",
        X9ObjectIdentifiers.id_ecPublicKey, ECKey.class), RSA("RSA", PKCSObjectIdentifiers.rsaEncryption, RSAKey.class);

    private final String id;
    private final ASN1ObjectIdentifier oid;
    private final Class<?> type;

    private PhizKeyType(String id, ASN1ObjectIdentifier oid, Class<?> type) {
        this.id = id;
        this.oid = oid;
        this.type = type;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public ASN1ObjectIdentifier getOid() {
        return this.oid;
    }

    @Override
    public Class<?> getType() {
        return this.type;
    }
}
