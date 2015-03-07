package gov.hhs.onc.phiz.test.crypto.ssl.revocation;

import gov.hhs.onc.phiz.test.beans.PhizServerBean;
import io.netty.channel.ChannelOption;
import java.security.SecureRandom;
import java.util.Map;
import javax.annotation.Nonnegative;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.springframework.beans.factory.InitializingBean;

public interface PhizOcspServer extends InitializingBean, PhizServerBean {
    public Map<ChannelOption<?>, Object> getChannelOptions();

    public void setChannelOptions(Map<ChannelOption<?>, Object> channelOpts);

    @Nonnegative
    public int getMaxContentLength();

    public void setMaxContentLength(@Nonnegative int maxContentLen);

    public SecureRandom getSecureRandom();

    public void setSecureRandom(SecureRandom secureRandom);

    public AlgorithmIdentifier getSignatureAlgorithmId();

    public void setSignatureAlgorithmId(String sigAlgId);
}
