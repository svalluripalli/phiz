package gov.hhs.onc.phiz.crypto.impl;

import java.security.KeyPairGenerator;
import java.security.SecureRandom;

public class PhizKeyPairGeneratorFactoryBean extends AbstractPhizCryptoFactoryBean<KeyPairGenerator> {
    private int keySize;
    private SecureRandom secRand;

    public PhizKeyPairGeneratorFactoryBean() {
        super(KeyPairGenerator.class);
    }

    @Override
    public KeyPairGenerator getObject() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(this.type, this.prov);
        keyPairGen.initialize(this.keySize, this.secRand);

        return keyPairGen;
    }

    public int getKeySize() {
        return this.keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public SecureRandom getSecureRandom() {
        return this.secRand;
    }

    public void setSecureRandom(SecureRandom secRand) {
        this.secRand = secRand;
    }
}
