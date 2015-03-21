package gov.hhs.onc.phiz.crypto.utils;

import com.github.sebhoss.warnings.CompilerWarnings;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

public final class PhizCertificatePathUtils {
    private PhizCertificatePathUtils() {
    }

    @Nullable
    public static X509Certificate findRootCertificate(X509Certificate cert, PKIXBuilderParameters builderParams) throws CertStoreException, IOException {
        X509Certificate[] pathCerts = buildPath(cert, builderParams);

        return ((pathCerts != null) ? pathCerts[(pathCerts.length - 1)] : null);
    }

    @Nullable
    public static X509Certificate[] buildPath(X509Certificate cert, PKIXBuilderParameters builderParams) throws CertStoreException, IOException {
        List<X509Certificate> pathCerts = new ArrayList<>(builderParams.getMaxPathLength());
        pathCerts.add(cert);

        Set<TrustAnchor> trustAnchors = builderParams.getTrustAnchors();
        List<CertStore> certStores = builderParams.getCertStores();
        boolean foundTrustAnchor = false;
        X509Certificate pathCert;

        while (((pathCert = cert) != null) && !foundTrustAnchor && !isRootIssuer(pathCert)) {
            if ((foundTrustAnchor = ((cert = findTrustAnchorCertificate(pathCert, trustAnchors)) != null))
                || ((cert = findIssuerCertificate(pathCert, certStores)) != null)) {
                pathCerts.add(cert);
            } else {
                return null;
            }
        }

        return pathCerts.toArray(new X509Certificate[pathCerts.size()]);
    }

    @Nullable
    public static X509Certificate findTrustAnchorCertificate(X509Certificate cert, Set<TrustAnchor> trustAnchors) throws IOException {
        TrustAnchor trustAnchor = findTrustAnchor(cert, trustAnchors);

        return ((trustAnchor != null) ? trustAnchor.getTrustedCert() : null);
    }

    @Nullable
    public static TrustAnchor findTrustAnchor(X509Certificate cert, Set<TrustAnchor> trustAnchors) throws IOException {
        X509CertSelector issuerCertSelector = buildIssuerCertificateSelector(cert);
        X509Certificate trustAnchorCert;

        for (TrustAnchor trustAnchor : trustAnchors) {
            if (((trustAnchorCert = trustAnchor.getTrustedCert()) != null) && issuerCertSelector.match(trustAnchorCert)) {
                return trustAnchor;
            }
        }

        return null;
    }

    @Nullable
    public static X509Certificate findIssuerCertificate(X509Certificate cert, CertStore ... certStores) throws CertStoreException, IOException {
        return findIssuerCertificate(cert, Arrays.asList(certStores));
    }

    @Nullable
    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    public static X509Certificate findIssuerCertificate(X509Certificate cert, Iterable<CertStore> certStores) throws CertStoreException, IOException {
        return findCertificate(buildIssuerCertificateSelector(cert), certStores);
    }

    public static X509CertSelector buildIssuerCertificateSelector(X509Certificate cert) throws IOException {
        X509CertSelector issuerCertSelector = new X509CertSelector();
        issuerCertSelector.setSubject(X500Name.getInstance(cert.getIssuerX500Principal().getEncoded()).getEncoded());

        return issuerCertSelector;
    }

    @Nullable
    public static X509Certificate findCertificate(X509CertSelector certSelector, CertStore ... certStores) throws CertStoreException {
        return findCertificate(certSelector, Arrays.asList(certStores));
    }

    @Nullable
    public static X509Certificate findCertificate(X509CertSelector certSelector, Iterable<CertStore> certStores) throws CertStoreException {
        Iterator<X509Certificate> certIterator = findCertificates(certSelector, certStores).iterator();

        return (certIterator.hasNext() ? certIterator.next() : null);
    }

    public static ListOrderedSet<X509Certificate> findCertificates(X509CertSelector certSelector, CertStore ... certStores) throws CertStoreException {
        return findCertificates(certSelector, Arrays.asList(certStores));
    }

    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    public static ListOrderedSet<X509Certificate> findCertificates(X509CertSelector certSelector, Iterable<CertStore> certStores) throws CertStoreException {
        ListOrderedSet<X509Certificate> certs = new ListOrderedSet<>();

        for (CertStore certStore : certStores) {
            certs.addAll(((Collection<X509Certificate>) certStore.getCertificates(certSelector)));
        }

        return certs;
    }

    public static CertStore buildStore(X509Certificate ... certs) throws GeneralSecurityException {
        JcaCertStoreBuilder storeBuilder = new JcaCertStoreBuilder();

        for (X509Certificate cert : certs) {
            storeBuilder.addCertificate(new JcaX509CertificateHolder(cert));
        }

        return storeBuilder.build();
    }

    public static boolean isRootIssuer(X509Certificate cert) {
        return cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal());
    }
}
