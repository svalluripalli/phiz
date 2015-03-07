package gov.hhs.onc.phiz.crypto.utils;

import com.github.sebhoss.warnings.CompilerWarnings;
import gov.hhs.onc.phiz.crypto.PhizCryptoProviders;
import java.security.GeneralSecurityException;
import java.security.cert.CertStore;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.AnnotatedException;
import org.bouncycastle.jce.provider.CertPathValidatorUtilities;
import org.bouncycastle.x509.ExtendedPKIXBuilderParameters;

public final class PhizCertificatePathUtils extends CertPathValidatorUtilities {
    private PhizCertificatePathUtils() {
    }

    @Nullable
    public static X509Certificate findRootCertificate(ExtendedPKIXBuilderParameters builderParams, X509Certificate cert) {
        X509Certificate[] pathCerts = buildPath(builderParams, cert);

        return ((pathCerts != null) ? pathCerts[(pathCerts.length - 1)] : null);
    }

    @Nullable
    public static X509Certificate[] buildPath(ExtendedPKIXBuilderParameters builderParams, X509Certificate cert) {
        List<X509Certificate> pathCerts = new ArrayList<>(builderParams.getMaxPathLength());
        pathCerts.add(cert);

        Set<TrustAnchor> trustAnchors = builderParams.getTrustAnchors();
        boolean foundTrustAnchor = false;
        X509Certificate pathCert;

        while (((pathCert = cert) != null) && !foundTrustAnchor && !isSelfIssued(pathCert)) {
            if ((foundTrustAnchor = ((cert = findTrustAnchorCertificate(trustAnchors, pathCert)) != null))
                || ((cert = findIssuerCertificate(builderParams, pathCert)) != null)) {
                pathCerts.add(cert);
            } else {
                return null;
            }
        }

        return pathCerts.toArray(new X509Certificate[pathCerts.size()]);
    }

    @Nullable
    public static X509Certificate findTrustAnchorCertificate(Set<TrustAnchor> trustAnchors, X509Certificate cert) {
        TrustAnchor trustAnchor = findTrustAnchor(trustAnchors, cert);

        return ((trustAnchor != null) ? trustAnchor.getTrustedCert() : null);
    }

    @Nullable
    public static TrustAnchor findTrustAnchor(Set<TrustAnchor> trustAnchors, X509Certificate cert) {
        try {
            return findTrustAnchor(cert, trustAnchors, PhizCryptoProviders.BC_NAME);
        } catch (AnnotatedException ignored) {
        }

        return null;
    }

    @Nullable
    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    public static X509Certificate findIssuerCertificate(ExtendedPKIXBuilderParameters builderParams, X509Certificate cert) {
        try {
            Iterator<X509Certificate> issuerCertIter = ((Collection<X509Certificate>) findIssuerCerts(cert, builderParams)).iterator();

            return (issuerCertIter.hasNext() ? issuerCertIter.next() : null);
        } catch (AnnotatedException ignored) {
        }

        return null;
    }

    public static CertStore buildStore(X509Certificate ... certs) throws GeneralSecurityException {
        JcaCertStoreBuilder storeBuilder = new JcaCertStoreBuilder();

        for (X509Certificate cert : certs) {
            storeBuilder.addCertificate(new JcaX509CertificateHolder(cert));
        }

        return storeBuilder.build();
    }
}
