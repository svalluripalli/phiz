package gov.hhs.onc.phiz.crypto.impl;

import java.io.InputStream;
import java.security.KeyStore;
import javax.annotation.Nullable;
import org.springframework.core.io.FileSystemResource;

public class PhizKeyStoreFactoryBean extends AbstractPhizCryptoFactoryBean<KeyStore> {
    protected String pass;
    protected FileSystemResource resource;

    public PhizKeyStoreFactoryBean() {
        super(KeyStore.class);
    }

    @Override
    public KeyStore getObject() throws Exception {
        try (InputStream inStream = this.resource.getInputStream()) {
            return this.getObjectInternal(inStream, this.pass);
        }
    }

    protected KeyStore getObjectInternal(@Nullable InputStream inStream, @Nullable String pass) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(this.type, this.prov);
        keyStore.load(inStream, ((pass != null) ? pass.toCharArray() : null));

        return keyStore;
    }

    public void setPassword(String pass) {
        this.pass = pass;
    }

    public FileSystemResource getResource() {
        return this.resource;
    }

    public void setResource(FileSystemResource resource) {
        this.resource = resource;
    }
}
