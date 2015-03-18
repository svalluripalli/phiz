package gov.hhs.onc.phiz.crypto.ssl.constraints.impl;

import java.security.AlgorithmConstraints;
import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.Key;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component("constraintsPermitAll")
public class PermitAllConstraints implements AlgorithmConstraints {
    @Override
    public boolean permits(Set<CryptoPrimitive> primitives, Key key) {
        validatePrimitives(primitives);
        validateKey(key);

        return true;
    }

    @Override
    public boolean permits(Set<CryptoPrimitive> primitives, String algId, @Nullable AlgorithmParameters algParams) {
        validatePrimitives(primitives);
        validateAlgorithmId(algId);

        return true;
    }

    @Override
    public boolean permits(Set<CryptoPrimitive> primitives, String algId, Key key, @Nullable AlgorithmParameters algParams) {
        validatePrimitives(primitives);
        validateAlgorithmId(algId);
        validateKey(key);

        return true;
    }

    protected static void validateKey(@Nullable Key key) {
        if (key == null) {
            throw new IllegalArgumentException("SSL key must be specified.");
        }
    }

    protected static void validateAlgorithmId(@Nullable String algId) {
        if (StringUtils.isEmpty(algId)) {
            throw new IllegalArgumentException("SSL algorithm ID must be specified.");
        }
    }

    protected static void validatePrimitives(@Nullable Set<CryptoPrimitive> primitives) {
        if (CollectionUtils.isEmpty(primitives)) {
            throw new IllegalArgumentException("SSL primitive(s) must be specified.");
        }
    }
}
