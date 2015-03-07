package gov.hhs.onc.phiz.crypto.logging.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import gov.hhs.onc.phiz.logging.logstash.impl.AbstractPhizJsonSerializer;
import java.security.cert.X509Certificate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.stereotype.Component;

@Component("jsonSerializerCert")
public class CertificateJsonSerializer extends AbstractPhizJsonSerializer<X509Certificate> {
    private final static String ALT_NAMES_FIELD_NAME_SUFFIX = "_alt_names";
    
    private final static String ENCODED_FIELD_NAME = "encoded";
    private final static String OID_FIELD_NAME = "oid";
    
    private final static String VERSION_FIELD_NAME = "version";
    private final static String SUBJECT_FIELD_NAME = "subject";
    private final static String SUBJECT_ALT_NAMES_FIELD_NAME = SUBJECT_FIELD_NAME + ALT_NAMES_FIELD_NAME_SUFFIX;
    private final static String ISSUER_FIELD_NAME = "issuer";
    private final static String ISSUER_ALT_NAMES_FIELD_NAME = ISSUER_FIELD_NAME + ALT_NAMES_FIELD_NAME_SUFFIX;
    private final static String SERIAL_NUM_FIELD_NAME = "serial_number";
    private final static String KEY_USAGES_FIELD_NAME = "key_usages";
    private final static String EXT_KEY_USAGES_FIELD_NAME = "extended_" + KEY_USAGES_FIELD_NAME;
    
    private final static String VALID_FIELD_NAME = "valid";
    private final static String VALID_FROM_FIELD_NAME = "from";
    private final static String VALID_TO_FIELD_NAME = "to";
    
    private final static String SIG_FIELD_NAME = "signature";
    private final static String SIG_ALG_FIELD_NAME = "algorithm";
    
    private final static String EXTS_FIELD_NAME = "extensions";
    
    private final static String EXT_CRITICAL_FIELD_NAME = "critical";

    private final static long serialVersionUID = 0L;

    public CertificateJsonSerializer() {
        super(X509Certificate.class);
    }

    @Override
    protected void serializeFields(X509Certificate cert, JsonGenerator jsonGen, SerializerProvider serializerProv) throws Exception {
        Extensions certExts = new JcaX509CertificateHolder(cert).getExtensions();
        Set<ASN1ObjectIdentifier> certExtOids = Stream.of(certExts.getExtensionOIDs()).collect(Collectors.toCollection(LinkedHashSet::new));
        
        jsonGen.writeObjectField(VERSION_FIELD_NAME, cert.getVersion());

        serializeDnField(jsonGen, SUBJECT_FIELD_NAME, new X500Name(cert.getSubjectX500Principal().getName()));

        if (certExtOids.contains(Extension.subjectAlternativeName)) {
            //jsonGen.writeObjectField(SUBJECT_ALT_NAMES_FIELD_NAME, cert.getSubjectAlternativeNames());
        }
        
        serializeDnField(jsonGen, ISSUER_FIELD_NAME, new X500Name(cert.getIssuerX500Principal().getName()));
        
        jsonGen.writeObjectField(SERIAL_NUM_FIELD_NAME, cert.getSerialNumber());
        
        jsonGen.writeObjectFieldStart(VALID_FIELD_NAME);
        jsonGen.writeObjectField(VALID_FROM_FIELD_NAME, cert.getNotBefore());
        jsonGen.writeObjectField(VALID_TO_FIELD_NAME, cert.getNotAfter());
        jsonGen.writeEndObject();
        
        jsonGen.writeObjectFieldStart(SIG_FIELD_NAME);
        jsonGen.writeObjectField(SIG_ALG_FIELD_NAME, cert.getSigAlgName());
        jsonGen.writeObjectField(OID_FIELD_NAME, cert.getSigAlgOID());
        jsonGen.writeObjectField(ENCODED_FIELD_NAME, Hex.encodeHexString(cert.getSignature()));
        jsonGen.writeEndObject();
        
        jsonGen.writeArrayFieldStart(EXTS_FIELD_NAME);
        
        Extension certExt;
        
        for (ASN1ObjectIdentifier certExtOid : certExtOids) {
            jsonGen.writeStartObject();
            jsonGen.writeObjectField(OID_FIELD_NAME, certExtOid.getId());
            jsonGen.writeObjectField(EXT_CRITICAL_FIELD_NAME, (certExt = certExts.getExtension(certExtOid)).isCritical());
            jsonGen.writeObjectField(ENCODED_FIELD_NAME, Hex.encodeHexString(certExt.getEncoded()));
            jsonGen.writeEndObject();
        }
        
        jsonGen.writeEndArray();
    }
    
    private static void serializeDnField(JsonGenerator jsonGen, String dnFieldName, X500Name dn) throws Exception {
        jsonGen.writeObjectFieldStart(dnFieldName);

        Map<String, List<AttributeTypeAndValue>> rdnAttrMap = Stream.of(dn.getRDNs()).flatMap(rdn -> Stream.of(rdn.getTypesAndValues()))
            .collect(Collectors.groupingBy((AttributeTypeAndValue rdnAttr) -> BCStyle.INSTANCE.oidToDisplayName(rdnAttr.getType())));
        
        for (String rdnAttrName : rdnAttrMap.keySet()) {
            jsonGen.writeArrayFieldStart(rdnAttrName);
            
            for (AttributeTypeAndValue rdnAttr : rdnAttrMap.get(rdnAttrName)) {
                jsonGen.writeString(IETFUtils.valueToString(rdnAttr.getValue()));
            }
            
            jsonGen.writeEndArray();
        }

        jsonGen.writeEndObject();
    }
}
