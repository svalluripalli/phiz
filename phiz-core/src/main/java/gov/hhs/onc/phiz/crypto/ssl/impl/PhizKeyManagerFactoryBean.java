package gov.hhs.onc.phiz.crypto.ssl.impl;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyStoreBuilderParameters;

public class PhizKeyManagerFactoryBean extends AbstractPhizCryptoManagerFactoryBean<KeyManager, KeyStoreBuilderParameters> {
    public PhizKeyManagerFactoryBean() {
        super(KeyManager.class);
    }

    @Override
    public KeyManager getObject() throws Exception {
        KeyManagerFactory factory = KeyManagerFactory.getInstance(this.type, this.prov);
        factory.init(this.factoryParams);

        return factory.getKeyManagers()[0];
    }
}
