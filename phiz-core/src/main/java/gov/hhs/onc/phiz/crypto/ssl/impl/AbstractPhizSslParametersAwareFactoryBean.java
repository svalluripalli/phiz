package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.crypto.impl.AbstractPhizCryptoFactoryBean;
import javax.net.ssl.SSLParameters;

public abstract class AbstractPhizSslParametersAwareFactoryBean<T> extends AbstractPhizCryptoFactoryBean<T> {
    protected SSLParameters params;

    protected AbstractPhizSslParametersAwareFactoryBean(Class<T> objClass) {
        super(objClass);
    }

    public SSLParameters getParameters() {
        return this.params;
    }

    public void setParameters(SSLParameters params) {
        this.params = params;
    }
}
