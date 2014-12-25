package gov.hhs.onc.phiz.crypto.impl;

import java.security.SecureRandom;

public class PhizSecureRandomFactoryBean extends AbstractPhizCryptoFactoryBean<SecureRandom> {
    public PhizSecureRandomFactoryBean() {
        super(SecureRandom.class);
    }

    @Override
    public SecureRandom getObject() throws Exception {
        SecureRandom secureRandom = SecureRandom.getInstance(this.type, this.prov);
        secureRandom.nextBytes(new byte[1]);

        return secureRandom;
    }
}
