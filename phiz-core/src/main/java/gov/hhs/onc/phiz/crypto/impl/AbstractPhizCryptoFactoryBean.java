package gov.hhs.onc.phiz.crypto.impl;

import gov.hhs.onc.phiz.beans.factory.impl.AbstractPhizFactoryBean;

public abstract class AbstractPhizCryptoFactoryBean<T> extends AbstractPhizFactoryBean<T> {
    protected String prov;
    protected String type;

    protected AbstractPhizCryptoFactoryBean(Class<T> objClass) {
        super(objClass);
    }

    public String getProvider() {
        return this.prov;
    }

    public void setProvider(String prov) {
        this.prov = prov;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
