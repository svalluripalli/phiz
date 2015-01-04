package gov.hhs.onc.phiz.crypto.ssl.impl;

import gov.hhs.onc.phiz.crypto.impl.AbstractPhizCryptoFactoryBean;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class PhizSslContextFactoryBean extends AbstractPhizCryptoFactoryBean<SSLContext> {
    private KeyManager[] keyManagers;
    private SecureRandom secureRandom;
    private TrustManager[] trustManagers;

    public PhizSslContextFactoryBean() {
        super(SSLContext.class);
    }

    @Override
    public SSLContext getObject() throws Exception {
        SSLContext sslContext = SSLContext.getInstance(this.type, this.prov);
        sslContext.init(this.keyManagers, this.trustManagers, this.secureRandom);

        return sslContext;
    }

    public KeyManager[] getKeyManagers() {
        return this.keyManagers;
    }

    public void setKeyManagers(KeyManager[] keyManagers) {
        this.keyManagers = keyManagers;
    }

    public SecureRandom getSecureRandom() {
        return this.secureRandom;
    }

    public void setSecureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    public TrustManager[] getTrustManagers() {
        return this.trustManagers;
    }

    public void setTrustManagers(TrustManager[] trustManagers) {
        this.trustManagers = trustManagers;
    }
}
