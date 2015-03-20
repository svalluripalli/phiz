package gov.hhs.onc.phiz.test.crypto.ssl.revocation;

import gov.hhs.onc.phiz.test.beans.PhizHttpServer;
import java.security.SecureRandom;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface PhizOcspServer extends PhizHttpServer {
    public SecureRandom getSecureRandom();

    public void setSecureRandom(SecureRandom secureRandom);

    public AlgorithmIdentifier getSignatureAlgorithmId();

    public void setSignatureAlgorithmId(String sigAlgId);
}
