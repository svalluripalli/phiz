package gov.hhs.onc.phiz.xml.utils;

import javax.xml.namespace.QName;
import org.apache.commons.lang3.StringUtils;

public final class PhizXmlQnameUtils {
    public final static String REF_DELIM = ":";

    private PhizXmlQnameUtils() {
    }

    public static String toReferenceString(QName qname) {
        String prefix = qname.getPrefix();

        return ((!StringUtils.isBlank(prefix) ? (prefix + REF_DELIM) : StringUtils.EMPTY) + qname.getLocalPart());
    }
}
