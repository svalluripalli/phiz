package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.crypto.impl.AbstractPhizCryptoFactoryBean;
import java.security.KeyStore;
import javax.net.ssl.ManagerFactoryParameters;

public abstract class AbstractPhizSslManagerFactoryBean<T, U extends ManagerFactoryParameters> extends AbstractPhizCryptoFactoryBean<T> {
    protected KeyStore keyStore;

    protected AbstractPhizSslManagerFactoryBean(Class<T> objClass) {
        super(objClass);
    }

    protected abstract U buildFactoryParameters() throws Exception;

    public KeyStore getKeyStore() {
        return this.keyStore;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }
}
