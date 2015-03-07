package gov.hhs.onc.phiz.crypto;

public interface PhizCryptoTagId extends PhizCryptoId {
    @Override
    public default int getOrder() {
        return this.getTag();
    }

    public int getTag();
}
