package gov.hhs.onc.phiz.crypto.impl;

import gov.hhs.onc.phiz.crypto.PhizKeyStoreEntry;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import javax.annotation.Nullable;

public abstract class AbstractPhizKeyStoreEntry<T extends Entry> implements PhizKeyStoreEntry<T> {
    protected ProtectionParameter protection;

    @Override
    public void setPassword(@Nullable String pass) {
        this.setProtection(((pass != null) ? new PasswordProtection(pass.toCharArray()) : null));
    }

    @Nullable
    @Override
    public ProtectionParameter getProtection() {
        return this.protection;
    }

    @Override
    public void setProtection(@Nullable ProtectionParameter protection) {
        this.protection = protection;
    }
}
