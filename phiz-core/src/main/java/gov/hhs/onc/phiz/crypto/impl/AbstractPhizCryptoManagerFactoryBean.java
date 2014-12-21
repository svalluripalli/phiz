package gov.hhs.onc.phiz.crypto.impl;

import javax.net.ssl.ManagerFactoryParameters;

public abstract class AbstractPhizCryptoManagerFactoryBean<T, U extends ManagerFactoryParameters> extends AbstractPhizCryptoFactoryBean<T> {
    protected U factoryParams;
    
    protected AbstractPhizCryptoManagerFactoryBean(Class<T> objClass) {
        super(objClass);
    }

    public U getFactoryParameters() {
        return this.factoryParams;
    }

    public void setFactoryParameters(U factoryParams) {
        this.factoryParams = factoryParams;
    }
}
