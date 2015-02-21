package gov.hhs.onc.phiz.crypto.ssl.impl;

import java.security.KeyStore.Builder;
import java.security.KeyStore.PasswordProtection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyStoreBuilderParameters;

public class PhizKeyManagerFactoryBean extends AbstractPhizSslManagerFactoryBean<KeyManager, KeyStoreBuilderParameters> {
    protected String pass;

    public PhizKeyManagerFactoryBean() {
        super(KeyManager.class);
    }

    @Override
    public KeyManager getObject() throws Exception {
        KeyManagerFactory factory = KeyManagerFactory.getInstance(this.type, this.prov);
        factory.init(this.buildFactoryParameters());

        return factory.getKeyManagers()[0];
    }

    @Override
    protected KeyStoreBuilderParameters buildFactoryParameters() throws Exception {
        return new KeyStoreBuilderParameters(Builder.newInstance(this.keyStore, new PasswordProtection(((this.pass != null) ? this.pass.toCharArray() : null))));
    }

    public void setPassword(String pass) {
        this.pass = pass;
    }
}
