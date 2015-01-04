package gov.hhs.onc.phiz.crypto.ssl.impl;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class PhizTrustManagerFactoryBean extends AbstractPhizCryptoManagerFactoryBean<TrustManager, CertPathTrustManagerParameters> {
    public PhizTrustManagerFactoryBean() {
        super(TrustManager.class);
    }

    @Override
    public TrustManager getObject() throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(this.type, this.prov);
        factory.init(this.factoryParams);

        return factory.getTrustManagers()[0];
    }
}
