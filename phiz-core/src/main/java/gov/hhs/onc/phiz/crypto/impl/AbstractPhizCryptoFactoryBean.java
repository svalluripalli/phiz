package gov.hhs.onc.phiz.crypto.impl;

import gov.hhs.onc.phiz.beans.factory.impl.AbstractPhizFactoryBean;
import java.security.Provider;

public abstract class AbstractPhizCryptoFactoryBean<T> extends AbstractPhizFactoryBean<T> {
    protected Provider prov;
    protected String type;

    protected AbstractPhizCryptoFactoryBean(Class<T> objClass) {
        super(objClass);
    }

    public Provider getProvider() {
        return this.prov;
    }

    public void setProvider(Provider prov) {
        this.prov = prov;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
