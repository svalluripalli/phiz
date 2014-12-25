package gov.hhs.onc.phiz.crypto.impl;

import java.security.KeyStore.TrustedCertificateEntry;
import java.security.cert.Certificate;

public class TrustedCertificateKeyStoreEntry extends AbstractPhizKeyStoreEntry<TrustedCertificateEntry> {
    private Certificate cert;

    @Override
    public TrustedCertificateEntry toEntry() {
        return new TrustedCertificateEntry(this.cert);
    }

    public Certificate getCertificate() {
        return this.cert;
    }

    public void setCertificate(Certificate cert) {
        this.cert = cert;
    }
}
