package gov.hhs.onc.phiz.web.test.utils;

import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.GroovyUtilsPro;
import java.util.Objects;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.ws.addressing.Names;

public final class PhizSoapUiUtils {
    public final static String DECL_NS_XPATH_PREFIX = "declare namespace ";
    public final static String DECL_NS_XPATH_DELIM = "='";
    public final static String DECL_NS_XPATH_SUFFIX = "';";

    public final static String WSA_DECL_NS_XPATH = DECL_NS_XPATH_PREFIX + JAXWSAConstants.WSA_PREFIX + DECL_NS_XPATH_DELIM + Names.WSA_NAMESPACE_NAME
        + DECL_NS_XPATH_SUFFIX;

    public final static String WSA_ELEM_XPATH_PREFIX = WSA_DECL_NS_XPATH + " //" + JAXWSAConstants.WSA_PREFIX + ":";
    public final static String WSA_MSG_ID_ELEM_XPATH = WSA_ELEM_XPATH_PREFIX + Names.WSA_MESSAGEID_NAME;
    public final static String WSA_RELATES_TO_ELEM_XPATH = WSA_ELEM_XPATH_PREFIX + Names.WSA_RELATESTO_NAME;

    private PhizSoapUiUtils() {
    }

    public static void assertAddressingMessageIdsMatch(PropertyExpansionContext propExpansionContext, MessageExchange msgExchange) throws Exception {
        GroovyUtilsPro groovyUtils = createGroovyUtils(propExpansionContext);

        assert Objects.equals(groovyUtils.getXmlHolder(msgExchange.getRequestContentAsXml()).getNodeValue(WSA_MSG_ID_ELEM_XPATH),
            groovyUtils.getXmlHolder(msgExchange.getResponseContentAsXml()).getNodeValue(WSA_RELATES_TO_ELEM_XPATH));
    }

    public static GroovyUtilsPro createGroovyUtils(PropertyExpansionContext propExpansionContext) {
        return new GroovyUtilsPro(propExpansionContext);
    }
}
