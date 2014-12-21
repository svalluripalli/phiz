package gov.hhs.onc.phiz.crypto.impl;

import java.security.KeyStore;
import org.springframework.core.io.Resource;

public class PhizKeyStoreFactoryBean extends AbstractPhizCryptoFactoryBean<KeyStore> {
    private String pass;
    private Resource resource;

    public PhizKeyStoreFactoryBean() {
        super(KeyStore.class);
    }

    @Override
    public KeyStore getObject() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(this.type, this.prov);
        keyStore.load(this.resource.getInputStream(), ((this.pass != null) ? this.pass.toCharArray() : null));

        return keyStore;
    }

    public void setPassword(String pass) {
        this.pass = pass;
    }

    public Resource getResource() {
        return this.resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
