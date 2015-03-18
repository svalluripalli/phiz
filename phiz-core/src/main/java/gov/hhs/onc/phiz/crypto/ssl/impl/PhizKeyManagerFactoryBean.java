package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.crypto.impl.AbstractPhizCryptoFactoryBean;
import gov.hhs.onc.phiz.crypto.ssl.PhizSslManagerBean;
import java.security.KeyStore;
import java.security.KeyStore.Builder;
import java.security.KeyStore.PasswordProtection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyStoreBuilderParameters;
import javax.net.ssl.X509ExtendedKeyManager;

public class PhizKeyManagerFactoryBean extends AbstractPhizCryptoFactoryBean<X509ExtendedKeyManager> implements PhizSslManagerBean<KeyStoreBuilderParameters> {
    private KeyStoreBuilderParameters builderParams;
    private KeyStore keyStore;
    private String pass;

    public PhizKeyManagerFactoryBean() {
        super(X509ExtendedKeyManager.class);
    }

    @Override
    public X509ExtendedKeyManager getObject() throws Exception {
        KeyManagerFactory factory = KeyManagerFactory.getInstance(this.type, this.prov);
        factory.init(this.builderParams);

        return ((X509ExtendedKeyManager) factory.getKeyManagers()[0]);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.builderParams =
            new KeyStoreBuilderParameters(Builder.newInstance(this.keyStore, new PasswordProtection(((this.pass != null) ? this.pass.toCharArray() : null))));
    }

    @Override
    public KeyStoreBuilderParameters getBuilderParameters() {
        return this.builderParams;
    }

    @Override
    public KeyStore getKeyStore() {
        return this.keyStore;
    }

    @Override
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public void setPassword(String pass) {
        this.pass = pass;
    }
}
