package gov.hhs.onc.phiz.xml.utils;

import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

public final class PhizXmlQnameUtils {
    public final static String XML_TYPE_ANNO_NS_URI_DEFAULT = ((String) AnnotationUtils.getDefaultValue(XmlType.class, "namespace"));

    public final static String REF_DELIM = ":";

    private PhizXmlQnameUtils() {
    }

    public static boolean hasAnnotatedQname(Object obj) {
        return (obj.getClass().getAnnotation(XmlType.class) != null);
    }

    public static QName getAnnotatedQname(Object obj) {
        Class<?> objClass = obj.getClass();
        XmlType objXmlTypeAnno = objClass.getAnnotation(XmlType.class);
        String objXmlNsUri = objXmlTypeAnno.namespace(), objXmlPrefix = XMLConstants.DEFAULT_NS_PREFIX;

        if (objXmlNsUri.equals(XML_TYPE_ANNO_NS_URI_DEFAULT)) {
            XmlSchema objPkgXmlSchemaAnno = objClass.getPackage().getAnnotation(XmlSchema.class);

            if (objPkgXmlSchemaAnno != null) {
                final String objPkgXmlNsUri = objXmlNsUri = objPkgXmlSchemaAnno.namespace();
                objXmlPrefix =
                    Stream.of(objPkgXmlSchemaAnno.xmlns()).filter((objPkgXmlNs) -> objPkgXmlNs.namespaceURI().equals(objPkgXmlNsUri)).map(XmlNs::prefix)
                        .findFirst().orElse(objXmlPrefix);
            } else {
                objXmlNsUri = XMLConstants.XML_NS_URI;
                objXmlPrefix = XMLConstants.XML_NS_PREFIX;
            }
        }

        return new QName(objXmlNsUri, objXmlTypeAnno.name(), objXmlPrefix);
    }

    public static String toReferenceString(QName qname) {
        String prefix = qname.getPrefix();

        return ((!StringUtils.isBlank(prefix) ? (prefix + REF_DELIM) : StringUtils.EMPTY) + qname.getLocalPart());
    }
}
