package gov.hhs.onc.phiz.test.crypto.ssl.revocation.impl;

import gov.hhs.onc.phiz.crypto.PhizCredential;
import gov.hhs.onc.phiz.crypto.ssl.revocation.OcspCertificateStatusType;
import gov.hhs.onc.phiz.crypto.ssl.revocation.OcspContentTypes;
import gov.hhs.onc.phiz.crypto.ssl.revocation.OcspResponseStatusType;
import gov.hhs.onc.phiz.crypto.ssl.revocation.impl.PhizCertificateId;
import gov.hhs.onc.phiz.crypto.utils.PhizCryptoUtils;
import gov.hhs.onc.phiz.logging.logstash.PhizLogstashTags;
import gov.hhs.onc.phiz.logging.logstash.impl.PhizLogstashMarkers;
import gov.hhs.onc.phiz.test.beans.impl.AbstractPhizHttpServer;
import gov.hhs.onc.phiz.test.crypto.ssl.revocation.PhizOcspServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPRespBuilder;
import org.bouncycastle.cert.ocsp.RespID;
import org.bouncycastle.cert.ocsp.jcajce.JcaRespID;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcContentSignerBuilder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.MimeType;

public class PhizOcspServerImpl extends AbstractPhizHttpServer implements PhizOcspServer {
    private class PhizOcspCredentialWrapper {
        private PhizOcspCredentialWrapper issuerCredWrapper;
        private PhizCredential cred;
        private X509Certificate cert;
        private X509CertificateHolder certHolder;
        private PhizCertificateId certId;
        private CertificateStatus certStatus;
        private RespID responderId;
        private ContentSigner contentSigner;

        public PhizOcspCredentialWrapper(PhizCredential cred) throws CertificateEncodingException, IOException, OperatorCreationException, OCSPException {
            this(null, cred);
        }

        public PhizOcspCredentialWrapper(@Nullable PhizOcspCredentialWrapper issuerCredWrapper, PhizCredential cred) throws CertificateEncodingException,
            IOException, OperatorCreationException, OCSPException {
            // noinspection ConstantConditions
            this.certHolder = new JcaX509CertificateHolder((this.cert = (this.cred = cred).getCertificate()));

            boolean credIssuer = this.cred.isIssuer();

            // noinspection ConstantConditions
            this.certId =
                new PhizCertificateId(PhizOcspServerImpl.this.digestCalc, (credIssuer
                    ? this.certHolder : (this.issuerCredWrapper = issuerCredWrapper).getCertificateHolder()), this.cert.getSerialNumber());

            this.certStatus = this.cred.getCertificateStatus();

            if (credIssuer) {
                // noinspection ConstantConditions
                this.responderId = new JcaRespID(this.cert.getSubjectX500Principal());
                // noinspection ConstantConditions
                this.contentSigner = PhizOcspServerImpl.this.contentSignerBuilder.build(PrivateKeyFactory.createKey(this.cred.getPrivateKey().getEncoded()));
            }
        }

        public X509Certificate getCertificate() {
            return this.cert;
        }

        public X509CertificateHolder getCertificateHolder() {
            return this.certHolder;
        }

        public PhizCertificateId getCertificateId() {
            return this.certId;
        }

        @Nullable
        public CertificateStatus getCertificateStatus() {
            return this.certStatus;
        }

        @Nullable
        public ContentSigner getContentSigner() {
            return this.contentSigner;
        }

        public PhizCredential getCredential() {
            return this.cred;
        }

        @Nullable
        public PhizOcspCredentialWrapper getIssuerCredentialWrapper() {
            return this.issuerCredWrapper;
        }

        @Nullable
        public RespID getResponderId() {
            return this.responderId;
        }
    }

    private class PhizOcspServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private Extensions exts;
        private PhizOcspCredentialWrapper credWrapper;
        private PhizOcspCredentialWrapper issuerCredWrapper;

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
            LOGGER.error("Unable to process OCSP request.", cause);

            if ((this.exts == null) || (this.credWrapper == null) || (this.issuerCredWrapper == null)) {
                this.writeResponse(context, HttpResponseStatus.INTERNAL_SERVER_ERROR);

                return;
            }

            // noinspection ConstantConditions
            BasicOCSPRespBuilder respBuilder = new BasicOCSPRespBuilder(this.credWrapper.getIssuerCredentialWrapper().getResponderId());
            respBuilder.setResponseExtensions(this.exts);

            // noinspection ConstantConditions
            this.writeResponse(
                context,
                RESP_WRAPPER_BUILDER.build(OcspResponseStatusType.INTERNAL_ERROR.getTag(),
                    respBuilder.build(this.issuerCredWrapper.getContentSigner(), ArrayUtils.toArray(this.credWrapper.getCertificateHolder()), new Date()))
                    .getEncoded());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext context, FullHttpRequest reqMsg) throws Exception {
            synchronized (PhizOcspServerImpl.this) {
                if (!PhizOcspServerImpl.this.credsInitialized) {
                    PhizOcspServerImpl.this.issuerCredWrappers = new HashMap<>();
                    PhizOcspServerImpl.this.credWrappers = new HashMap<>();

                    PhizOcspCredentialWrapper credWrapper;

                    for (PhizCredential issuerCred : PhizOcspServerImpl.this.creds.stream().filter(PhizCredential::isIssuer).toArray(PhizCredential[]::new)) {
                        PhizOcspServerImpl.this.issuerCredWrappers.put((credWrapper = new PhizOcspCredentialWrapper(issuerCred)).getCertificate(), credWrapper);
                    }

                    for (PhizCredential cred : PhizOcspServerImpl.this.creds.stream().filter(((Predicate<PhizCredential>) PhizCredential::isIssuer).negate())
                        .toArray(PhizCredential[]::new)) {
                        // noinspection ConstantConditions
                        PhizOcspServerImpl.this.credWrappers
                            .put(
                                (credWrapper =
                                    new PhizOcspCredentialWrapper(PhizOcspServerImpl.this.issuerCredWrappers.get(cred.getIssuerCredential().getCertificate()),
                                        cred)).getCertificateId(), credWrapper);
                    }

                    PhizOcspServerImpl.this.credsInitialized = true;
                }
            }

            if (!reqMsg.getMethod().equals(HttpMethod.POST)) {
                this.writeResponse(context, HttpResponseStatus.METHOD_NOT_ALLOWED);

                return;
            }

            HttpHeaders reqMsgHeaders = reqMsg.headers();

            if (!reqMsgHeaders.contains(Names.CONTENT_TYPE) || !MimeType.valueOf(reqMsgHeaders.get(Names.CONTENT_TYPE)).equals(OcspContentTypes.OCSP_REQ)) {
                this.writeResponse(context, HttpResponseStatus.BAD_REQUEST);

                return;
            }

            OCSPReq req = new OCSPReq(reqMsg.content().copy().array());
            this.exts = new Extensions(req.getExtension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce));

            PhizCertificateId reqCertId = new PhizCertificateId(req.getRequestList()[0].getCertID()), certId = null;
            DigestCalculator reqDigestCalc = PhizCryptoUtils.DIGEST_CALC_PROV.get(new AlgorithmIdentifier(reqCertId.getHashAlgOID()));
            BigInteger certSerialNum = reqCertId.getSerialNumber();

            for (PhizCertificateId availableCertId : PhizOcspServerImpl.this.credWrappers.keySet()) {
                if (reqCertId.matches(availableCertId)) {
                    certId = availableCertId;

                    break;
                }
            }

            if (certId == null) {
                throw new OCSPException(String.format("Unable to match OCSP request certificate (serialNum=%d).", certSerialNum));
            }

            this.issuerCredWrapper = (this.credWrapper = PhizOcspServerImpl.this.credWrappers.get(certId)).getIssuerCredentialWrapper();

            X509Certificate cert = credWrapper.getCertificate();
            String certSubjectDnName = cert.getSubjectX500Principal().getName(), certIssuerDnName = cert.getIssuerX500Principal().getName();
            CertificateStatus certStatus = credWrapper.getCertificateStatus();

            // noinspection ConstantConditions
            BasicOCSPRespBuilder respBuilder = new BasicOCSPRespBuilder(issuerCredWrapper.getResponderId());
            respBuilder.setResponseExtensions(this.exts);
            respBuilder.addResponse(certId, certStatus);

            // noinspection ConstantConditions
            this.writeResponse(
                context,
                RESP_WRAPPER_BUILDER.build(OcspResponseStatusType.SUCCESSFUL.getTag(),
                    respBuilder.build(issuerCredWrapper.getContentSigner(), ArrayUtils.toArray(credWrapper.getCertificateHolder()), new Date())).getEncoded());

            LOGGER.debug(PhizLogstashMarkers.append(PhizLogstashTags.SSL), String.format(
                "Wrote OCSP response (status=%s) certificate (subjectDnName=%s, issuerDnName=%s, serialNum=%d) response (status=%s).",
                OcspResponseStatusType.SUCCESSFUL.name(), certSubjectDnName, certIssuerDnName, certSerialNum,
                PhizCryptoUtils.findByType(OcspCertificateStatusType.class, ((certStatus != null) ? certStatus.getClass() : CertificateStatus.class))));
        }

        private void writeResponse(ChannelHandlerContext context, HttpResponseStatus respMsgStatus) {
            this.writeResponse(context, respMsgStatus, Unpooled.EMPTY_BUFFER);
        }

        private void writeResponse(ChannelHandlerContext context, byte[] respContent) {
            this.writeResponse(context, HttpResponseStatus.OK, Unpooled.wrappedBuffer(respContent));
        }

        private void writeResponse(ChannelHandlerContext context, HttpResponseStatus respMsgStatus, ByteBuf respContentBuffer) {
            FullHttpResponse respMsg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, respMsgStatus, respContentBuffer);

            HttpHeaders.setContentLength(respMsg, respContentBuffer.array().length);
            respMsg.headers().set(Names.CONTENT_TYPE, OcspContentTypes.OCSP_RESP.toString());

            context.writeAndFlush(respMsg).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private final static OCSPRespBuilder RESP_WRAPPER_BUILDER = new OCSPRespBuilder();

    private final static Logger LOGGER = LoggerFactory.getLogger(PhizOcspServerImpl.class);

    @Autowired
    @Lazy
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    private List<PhizCredential> creds;

    private SecureRandom secureRandom;
    private AlgorithmIdentifier sigAlgId;
    private AlgorithmIdentifier digestAlgId;
    private DigestCalculator digestCalc;
    private BcContentSignerBuilder contentSignerBuilder;
    private boolean credsInitialized;
    private Map<X509Certificate, PhizOcspCredentialWrapper> issuerCredWrappers;
    private Map<PhizCertificateId, PhizOcspCredentialWrapper> credWrappers;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.digestCalc = PhizCryptoUtils.DIGEST_CALC_PROV.get((this.digestAlgId = PhizCryptoUtils.DIGEST_ALG_ID_FINDER.find(this.sigAlgId)));

        this.contentSignerBuilder = new BcRSAContentSignerBuilder(this.sigAlgId, this.digestAlgId);
        this.contentSignerBuilder.setSecureRandom(this.secureRandom);
    }

    @Override
    protected void initializePipeline(ChannelPipeline channelPipeline) {
        super.initializePipeline(channelPipeline);

        channelPipeline.addLast(new PhizOcspServerHandler());
    }

    @Override
    public SecureRandom getSecureRandom() {
        return this.secureRandom;
    }

    @Override
    public void setSecureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    @Override
    public AlgorithmIdentifier getSignatureAlgorithmId() {
        return this.sigAlgId;
    }

    @Override
    public void setSignatureAlgorithmId(String sigAlgId) {
        this.sigAlgId = PhizCryptoUtils.SIG_ALG_ID_FINDER.find(sigAlgId);
    }
}
