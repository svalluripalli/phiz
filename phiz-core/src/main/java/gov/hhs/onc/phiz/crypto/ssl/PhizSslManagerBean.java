package gov.hhs.onc.phiz.crypto.ssl;

import gov.hhs.onc.phiz.crypto.PhizCryptoServiceBean;
import java.security.KeyStore;
import org.springframework.beans.factory.InitializingBean;

public interface PhizSslManagerBean<T> extends InitializingBean, PhizCryptoServiceBean {
    public T getBuilderParameters();

    public KeyStore getKeyStore();

    public void setKeyStore(KeyStore keyStore);
}
