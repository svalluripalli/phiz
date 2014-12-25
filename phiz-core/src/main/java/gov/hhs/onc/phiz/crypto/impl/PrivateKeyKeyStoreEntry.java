package gov.hhs.onc.phiz.crypto.impl;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class PrivateKeyKeyStoreEntry extends AbstractPhizKeyStoreEntry<PrivateKeyEntry> {
    private Certificate[] certChain;
    private PrivateKey privateKey;

    @Override
    public PrivateKeyEntry toEntry() {
        return new PrivateKeyEntry(this.privateKey, this.certChain);
    }

    public Certificate[] getCertificateChain() {
        return certChain;
    }

    public void setCertificateChain(Certificate[] certChain) {
        this.certChain = certChain;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}
