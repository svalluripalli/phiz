package gov.hhs.onc.phiz.crypto.ssl.impl;

import java.security.cert.CertSelector;
import java.security.cert.PKIXBuilderParameters;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class PhizTrustManagerFactoryBean extends AbstractPhizSslManagerFactoryBean<TrustManager, CertPathTrustManagerParameters> {
    private CertSelector certSelector;

    public PhizTrustManagerFactoryBean() {
        super(TrustManager.class);
    }

    @Override
    public TrustManager getObject() throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(this.type, this.prov);
        factory.init(this.buildFactoryParameters());

        return factory.getTrustManagers()[0];
    }

    @Override
    protected CertPathTrustManagerParameters buildFactoryParameters() throws Exception {
        PKIXBuilderParameters builderParams = new PKIXBuilderParameters(this.keyStore, this.certSelector);
        builderParams.setRevocationEnabled(false);

        return new CertPathTrustManagerParameters(builderParams);
    }

    public CertSelector getCertSelector() {
        return this.certSelector;
    }

    public void setCertSelector(CertSelector certSelector) {
        this.certSelector = certSelector;
    }
}
