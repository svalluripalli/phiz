package gov.hhs.onc.phiz.web.test.soapui.utils;

import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.GroovyUtils;
import com.eviware.soapui.support.XmlHolder;
import gov.hhs.onc.phiz.xml.PhizXmlNs;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.xml.soap.SOAPConstants;
import org.apache.commons.collections4.MapUtils;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.ws.addressing.Names;

public final class PhizSoapUiUtils {
    public final static Map<String, String> DEFAULT_XML_NS_DECL = new HashMap<String, String>() {
        private final static long serialVersionUID = 0L;

        {
            this.put("soap", SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
            this.put(JAXWSAConstants.WSA_PREFIX, Names.WSA_NAMESPACE_NAME);
            this.put(PhizXmlNs.IIS_PREFIX, PhizXmlNs.IIS);
            this.put(PhizXmlNs.IIS_HUB_PREFIX, PhizXmlNs.IIS_HUB);
        }
    };

    public final static String WSA_ELEM_XPATH_PREFIX = "//" + JAXWSAConstants.WSA_PREFIX + ":";

    public final static String WSA_MSG_ID_ELEM_XPATH = WSA_ELEM_XPATH_PREFIX + Names.WSA_MESSAGEID_NAME;
    public final static String WSA_RELATES_TO_ELEM_XPATH = WSA_ELEM_XPATH_PREFIX + Names.WSA_RELATESTO_NAME;

    private PhizSoapUiUtils() {
    }

    public static void assertAddressingMessageIdsMatch(PropertyExpansionContext propExpansionContext, MessageExchange msgExchange) throws Exception {
        GroovyUtils groovyUtils = buildGroovyUtils(propExpansionContext);

        assertNodeValuesMatch(buildXmlHolder(groovyUtils, msgExchange.getRequestContentAsXml()).getNodeValue(WSA_MSG_ID_ELEM_XPATH),
            buildXmlHolder(groovyUtils, msgExchange.getResponseContentAsXml()).getNodeValue(WSA_RELATES_TO_ELEM_XPATH));
    }

    public static void assertNodeValuesMatch(PropertyExpansionContext propExpansionContext, String xml, String xpath, @Nullable Object expectedNodeValues)
        throws Exception {
        assertNodeValuesMatch(propExpansionContext, xml, null, xpath, expectedNodeValues);
    }

    public static void assertNodeValuesMatch(PropertyExpansionContext propExpansionContext, String xml, @Nullable Map<String, String> xmlNsDecl, String xpath,
        @Nullable Object expectedNodeValues) throws Exception {
        assertNodeValuesMatch(buildNodeValues(propExpansionContext, xml, xmlNsDecl, xpath), expectedNodeValues);
    }

    public static void assertNodeValuesMatch(@Nullable Object nodeValues, @Nullable Object expectedNodeValues) throws Exception {
        assert Objects.deepEquals(nodeValues, expectedNodeValues);
    }

    @Nullable
    public static Object buildNodeValues(PropertyExpansionContext propExpansionContext, String xml, String xpath) throws Exception {
        return buildNodeValues(propExpansionContext, xml, null, xpath);
    }

    @Nullable
    public static Object buildNodeValues(PropertyExpansionContext propExpansionContext, String xml, @Nullable Map<String, String> xmlNsDecl, String xpath)
        throws Exception {
        return buildXmlHolder(buildGroovyUtils(propExpansionContext), xml, xmlNsDecl).get(xpath);
    }

    public static XmlHolder buildXmlHolder(GroovyUtils groovyUtils, String xml) throws Exception {
        return buildXmlHolder(groovyUtils, xml, null);
    }

    public static XmlHolder buildXmlHolder(GroovyUtils groovyUtils, String xml, @Nullable Map<String, String> xmlNsDecl) throws Exception {
        XmlHolder xmlHolder = groovyUtils.getXmlHolder(xml);

        DEFAULT_XML_NS_DECL.forEach(xmlHolder::declareNamespace);

        if (!MapUtils.isEmpty(xmlNsDecl)) {
            xmlNsDecl.forEach(xmlHolder::declareNamespace);
        }

        return xmlHolder;
    }

    public static GroovyUtils buildGroovyUtils(PropertyExpansionContext propExpansionContext) {
        return new GroovyUtils(propExpansionContext);
    }
}
