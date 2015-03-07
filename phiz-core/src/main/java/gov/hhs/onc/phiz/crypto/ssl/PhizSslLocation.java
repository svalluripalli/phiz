package gov.hhs.onc.phiz.crypto.ssl;

import gov.hhs.onc.phiz.crypto.PhizCryptoId;

public enum PhizSslLocation implements PhizCryptoId {
    CLIENT, SERVER;

    private final String id;

    private PhizSslLocation() {
        this.id = this.name().toLowerCase();
    }

    @Override
    public String getId() {
        return this.id;
    }
}
