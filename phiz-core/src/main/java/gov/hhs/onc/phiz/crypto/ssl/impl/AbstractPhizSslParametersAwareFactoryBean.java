package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.crypto.impl.AbstractPhizCryptoFactoryBean;
import javax.net.ssl.SSLParameters;

public abstract class AbstractPhizSslParametersAwareFactoryBean<T> extends AbstractPhizCryptoFactoryBean<T> {
    protected SSLParameters sslParams;

    protected AbstractPhizSslParametersAwareFactoryBean(Class<T> objClass) {
        super(objClass);
    }

    public SSLParameters getSslParameters() {
        return this.sslParams;
    }

    public void setSslParameters(SSLParameters sslParams) {
        this.sslParams = sslParams;
    }
}
