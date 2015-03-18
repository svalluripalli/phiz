package gov.hhs.onc.phiz.crypto;

import java.security.Provider;

public interface PhizCryptoServiceBean {
    public Provider getProvider();

    public void setProvider(Provider prov);

    public String getType();

    public void setType(String type);
}
