package gov.hhs.onc.phiz.crypto.impl;

import java.security.KeyPairGenerator;
import java.security.SecureRandom;

public class PhizKeyPairGeneratorFactoryBean extends AbstractPhizCryptoFactoryBean<KeyPairGenerator> {
    private int keySize;
    private SecureRandom secRand;

    public PhizKeyPairGeneratorFactoryBean() {
        this(-1);
    }

    public PhizKeyPairGeneratorFactoryBean(int keySize) {
        super(KeyPairGenerator.class);

        this.keySize = keySize;
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

    public SecureRandom getSecureRandom() {
        return this.secRand;
    }

    public void setSecureRandom(SecureRandom secRand) {
        this.secRand = secRand;
    }
}
