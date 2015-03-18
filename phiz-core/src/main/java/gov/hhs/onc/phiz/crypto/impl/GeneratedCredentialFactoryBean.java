package gov.hhs.onc.phiz.crypto.impl;

import br.net.woodstock.rockframework.security.Identity;
import br.net.woodstock.rockframework.security.cert.CertificateGenerator;
import br.net.woodstock.rockframework.security.cert.CertificateRequest;
import br.net.woodstock.rockframework.security.cert.CertificateResponse;
import gov.hhs.onc.phiz.crypto.PhizCredential;
import java.io.OutputStreamWriter;
import java.security.KeyPairGenerator;
import java.util.Date;
import javax.annotation.Nullable;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;

public class GeneratedCredentialFactoryBean extends AbstractPhizCryptoFactoryBean<PhizCredential> implements BeanFactoryAware {
    @Autowired
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    private CertificateGenerator certGen;

    private ListableBeanFactory beanFactory;
    private PhizCredential cred;
    private String keyPairGenBeanName;
    private FileSystemResource privateKeyResource;
    private FileSystemResource certResource;

    public GeneratedCredentialFactoryBean() {
        super(PhizCredential.class);
    }

    @Override
    public PhizCredential getObject() throws Exception {
        CertificateRequest certReq = this.cred.getCertificateRequest();

        certReq.setKeyPair(((KeyPairGenerator) this.beanFactory.getBean(this.keyPairGenBeanName, certReq.getKeySize().getSize())).generateKeyPair());

        if (!this.cred.isRootIssuer()) {
            // noinspection ConstantConditions
            this.cred.getCertificateRequest().setIssuer(this.cred.getIssuerCredential().getCertificateResponse().getIdentity());
        }

        CertificateResponse certResp = this.certGen.generate(certReq);
        this.cred.setCertificateResponse(certResp);

        Identity identity = certResp.getIdentity();

        if (this.hasPrivateKeyResource()) {
            // noinspection ConstantConditions
            try (PemWriter privateKeyWriter = new PemWriter(new OutputStreamWriter(this.privateKeyResource.getOutputStream()))) {
                privateKeyWriter.writeObject(new JcaPKCS8Generator(identity.getPrivateKey(), null));
                privateKeyWriter.flush();
            }
        }

        if (this.hasCertificateResource()) {
            // noinspection ConstantConditions
            try (PemWriter certWriter = new PemWriter(new OutputStreamWriter(this.certResource.getOutputStream()))) {
                certWriter.writeObject(new JcaMiscPEMGenerator(identity.getChain().get(0)));
                certWriter.flush();
            }
        }

        if (this.cred.isRevoked() && (this.cred.getRevocationTime() == null)) {
            this.cred.setRevocationTime(new Date());
        }

        return this.cred;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = ((ListableBeanFactory) beanFactory);
    }

    public boolean hasCertificateResource() {
        return (this.certResource != null);
    }

    @Nullable
    public FileSystemResource getCertificateResource() {
        return this.certResource;
    }

    public void setCertificateResource(@Nullable FileSystemResource certResource) {
        this.certResource = certResource;
    }

    public PhizCredential getCredential() {
        return this.cred;
    }

    public void setCredential(PhizCredential cred) {
        this.cred = cred;
    }

    public String getKeyPairGeneratorBeanName() {
        return this.keyPairGenBeanName;
    }

    public void setKeyPairGeneratorBeanName(String keyPairGenBeanName) {
        this.keyPairGenBeanName = keyPairGenBeanName;
    }

    public boolean hasPrivateKeyResource() {
        return (this.privateKeyResource != null);
    }

    @Nullable
    public FileSystemResource getPrivateKeyResource() {
        return this.privateKeyResource;
    }

    public void setPrivateKeyResource(@Nullable FileSystemResource privateKeyResource) {
        this.privateKeyResource = privateKeyResource;
    }
}
