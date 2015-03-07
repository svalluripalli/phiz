package gov.hhs.onc.phiz.crypto.utils;

import gov.hhs.onc.phiz.crypto.PhizCryptoTagId;
import gov.hhs.onc.phiz.crypto.PhizCryptoTypeId;
import java.util.EnumSet;
import javax.annotation.Nullable;
import org.apache.commons.lang3.ClassUtils;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.SignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.springframework.core.OrderComparator;

public final class PhizCryptoUtils {
    public final static SignatureAlgorithmIdentifierFinder SIG_ALG_ID_FINDER = new DefaultSignatureAlgorithmIdentifierFinder();

    public final static DigestAlgorithmIdentifierFinder DIGEST_ALG_ID_FINDER = new DefaultDigestAlgorithmIdentifierFinder();

    public final static DigestCalculatorProvider DIGEST_CALC_PROV = new BcDigestCalculatorProvider();

    private PhizCryptoUtils() {
    }

    @Nullable
    public static <T extends Enum<T> & PhizCryptoTagId> T findByTag(Class<T> enumClass, int tag) {
        return EnumSet.allOf(enumClass).stream().sorted(OrderComparator.INSTANCE).filter(enumItem -> (enumItem.getTag() == tag)).findFirst().orElse(null);
    }

    @Nullable
    public static <T extends Enum<T> & PhizCryptoTypeId> T findByType(Class<T> enumClass, Class<?> type) {
        return EnumSet.allOf(enumClass).stream().sorted(OrderComparator.INSTANCE).filter(enumItem -> ClassUtils.isAssignable(type, enumItem.getType()))
            .findFirst().orElse(null);
    }
}
