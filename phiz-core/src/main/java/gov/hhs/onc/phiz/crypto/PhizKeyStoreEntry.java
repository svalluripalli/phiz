package gov.hhs.onc.phiz.crypto;

import java.security.KeyStore.Entry;
import java.security.KeyStore.ProtectionParameter;
import javax.annotation.Nullable;

public interface PhizKeyStoreEntry<T extends Entry> {
    public T toEntry();

    public void setPassword(@Nullable String pass);

    @Nullable
    public ProtectionParameter getProtection();

    public void setProtection(@Nullable ProtectionParameter protection);
}
