package gov.hhs.onc.phiz.crypto.ssl.constraints.impl;

import gov.hhs.onc.phiz.crypto.PhizKeyType;
import gov.hhs.onc.phiz.crypto.ssl.PhizSslLocation;
import gov.hhs.onc.phiz.crypto.ssl.impl.AbstractPhizPathChecker;
import gov.hhs.onc.phiz.crypto.utils.PhizCryptoUtils;
import gov.hhs.onc.phiz.logging.logstash.PhizLogstashTags;
import gov.hhs.onc.phiz.logging.logstash.impl.PhizLogstashMarkers;
import java.math.BigInteger;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.crypto.interfaces.DHKey;
import javax.crypto.spec.DHParameterSpec;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhizConstraintsChecker extends AbstractPhizPathChecker {
    private final static Logger LOGGER = LoggerFactory.getLogger(PhizConstraintsChecker.class);

    private Set<PhizKeyType> keyTypes = EnumSet.noneOf(PhizKeyType.class);
    private Map<PhizKeyType, Integer> minKeySizes = new EnumMap<>(PhizKeyType.class);
    private Set<AlgorithmIdentifier> sigAlgIds = new HashSet<>();

    public PhizConstraintsChecker(PhizSslLocation loc, X509Certificate issuerCert) {
        super(loc, issuerCert, BasicReason.ALGORITHM_CONSTRAINED);
    }

    @Override
    @SuppressWarnings({ "CloneDoesntCallSuperClone" })
    public PhizConstraintsChecker clone() {
        return this;
    }

    @Override
    protected void checkInternal(X509Certificate cert, String certSubjectDnNameStr, String certIssuerDnNameStr, BigInteger certSerialNum)
        throws CertPathValidatorException {
        PublicKey certPublicKey = cert.getPublicKey();
        String certKeyAlgName = certPublicKey.getAlgorithm();
        PhizKeyType certKeyType = PhizCryptoUtils.findByType(PhizKeyType.class, certPublicKey.getClass());

        if (certKeyType == null) {
            throw this.buildException(String.format("Unknown SSL %s certificate (subjectDnName=%s, issuerDnName=%s, serialNum=%d) key type (algName=%s).",
                this.loc.getId(), certSubjectDnNameStr, certIssuerDnNameStr, certSerialNum, certKeyAlgName));
        }

        String certKeyAlgOid = certKeyType.getOid().getId();

        if (!this.keyTypes.contains(certKeyType)) {
            throw this.buildException(String.format(
                "Invalid SSL %s certificate (subjectDnName=%s, issuerDnName=%s, serialNum=%d) key type (algName=%s, algOid=%s).", this.loc.getId(),
                certSubjectDnNameStr, certIssuerDnNameStr, certSerialNum, certKeyAlgName, certKeyAlgOid));
        }

        if (this.minKeySizes.containsKey(certKeyType)) {
            int minKeySize = this.minKeySizes.get(certKeyType), certKeySize = extractKeySize(certKeyType, certPublicKey);

            if (certKeySize >= minKeySize) {
                LOGGER.debug(PhizLogstashMarkers.append(PhizLogstashTags.SSL), String.format(
                    "Valid SSL %s certificate (subjectDnName=%s, issuerDnName=%s, serialNum=%d) key (algName=%s, algOid=%s) size (%d >= %d).",
                    this.loc.getId(), certSubjectDnNameStr, certIssuerDnNameStr, certSerialNum, certKeyAlgName, certKeyAlgOid, certKeySize, minKeySize));
            } else if (certKeySize == -1) {
                throw this.buildException(String.format(
                    "Unknown SSL %s certificate (subjectDnName=%s, issuerDnName=%s, serialNum=%d) key (algName=%s, algOid=%s) size.", this.loc.getId(),
                    certSubjectDnNameStr, certIssuerDnNameStr, certSerialNum, certKeyAlgName, certKeyAlgOid));
            } else {
                throw this.buildException(String.format(
                    "Invalid SSL %s certificate (subjectDnName=%s, issuerDnName=%s, serialNum=%d) key (algName=%s, algOid=%s) size (%d < %d).",
                    this.loc.getId(), certSubjectDnNameStr, certIssuerDnNameStr, certSerialNum, certKeyAlgName, certKeyAlgOid, certKeySize, minKeySize));
            }
        }

        String certSigAlgName = cert.getSigAlgName(), certSigAlgOid = cert.getSigAlgOID();
        AlgorithmIdentifier certSigAlgId = PhizCryptoUtils.SIG_ALG_ID_FINDER.find(certSigAlgName);

        if (this.sigAlgIds.contains(certSigAlgId)) {
            LOGGER.debug(
                PhizLogstashMarkers.append(PhizLogstashTags.SSL),
                String.format("Valid SSL %s certificate (subjectDnName=%s, issuerDnName=%s, serialNum=%d) signature algorithm (name=%s, oid=%s).",
                    this.loc.getId(), certSubjectDnNameStr, certIssuerDnNameStr, certSerialNum, certSigAlgName, certSigAlgOid));
        } else {
            throw this.buildException(String.format(
                "Invalid SSL %s certificate (subjectDnName=%s, issuerDnName=%s, serialNum=%d) signature algorithm (name=%s, oid=%s).", this.loc.getId(),
                certSubjectDnNameStr, certIssuerDnNameStr, certSerialNum, certSigAlgName, certSigAlgOid));
        }
    }

    private static int extractKeySize(PhizKeyType keyType, Key key) {
        switch (keyType) {
            case DH:
                DHParameterSpec dhKeyParams = ((DHKey) key).getParams();
                int dhKeyParamLValue = dhKeyParams.getL();

                return ((dhKeyParamLValue != 0) ? dhKeyParamLValue : dhKeyParams.getP().bitLength());

            case DSA:
                return (((DSAKey) key).getParams().getP().bitLength() - 1);

            case EC:
                return ((ECKey) key).getParams().getOrder().bitLength();

            case RSA:
                return ((RSAKey) key).getModulus().bitLength();

            default:
                return -1;
        }
    }

    public Set<PhizKeyType> getKeyTypes() {
        return this.keyTypes;
    }

    public void setKeyTypes(Set<PhizKeyType> keyTypes) {
        this.keyTypes = keyTypes;
    }

    public Map<PhizKeyType, Integer> getMinimumKeySizes() {
        return this.minKeySizes;
    }

    public void setMinimumKeySizes(Map<PhizKeyType, Integer> minKeySizes) {
        this.minKeySizes.clear();
        this.minKeySizes.putAll(minKeySizes);
    }

    public Set<AlgorithmIdentifier> getSignatureAlgorithmIds() {
        return this.sigAlgIds;
    }

    public void setSignatureAlgorithmIds(Set<String> sigAlgIds) {
        this.sigAlgIds.clear();

        sigAlgIds.stream().map(PhizCryptoUtils.SIG_ALG_ID_FINDER::find).forEach(this.sigAlgIds::add);
    }
}
