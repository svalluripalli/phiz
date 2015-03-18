package gov.hhs.onc.phiz.crypto.impl;

import gov.hhs.onc.phiz.beans.factory.impl.AbstractPhizFactoryBean;
import gov.hhs.onc.phiz.crypto.PhizCryptoServiceBean;
import java.security.Provider;

public abstract class AbstractPhizCryptoFactoryBean<T> extends AbstractPhizFactoryBean<T> implements PhizCryptoServiceBean {
    protected Provider prov;
    protected String type;

    protected AbstractPhizCryptoFactoryBean(Class<T> objClass) {
        super(objClass);
    }

    @Override
    public Provider getProvider() {
        return this.prov;
    }

    @Override
    public void setProvider(Provider prov) {
        this.prov = prov;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
