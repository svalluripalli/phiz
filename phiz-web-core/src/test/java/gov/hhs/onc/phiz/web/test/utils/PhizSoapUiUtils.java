package gov.hhs.onc.phiz.web.test.utils;

import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.GroovyUtilsPro;
import java.util.Objects;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.ws.addressing.Names;

public final class PhizSoapUiUtils {
    private final static String WSA_XPATH_PREFIX = "declare namespace " + JAXWSAConstants.WSA_PREFIX + "='" + Names.WSA_NAMESPACE_NAME + "'; //"
        + JAXWSAConstants.WSA_PREFIX + ":";

    private final static String WSA_MSG_ID_XPATH = WSA_XPATH_PREFIX + Names.WSA_MESSAGEID_NAME;
    private final static String WSA_RELATES_TO_XPATH = WSA_XPATH_PREFIX + Names.WSA_RELATESTO_NAME;

    private PhizSoapUiUtils() {
    }

    public static void assertAddressingMessageIdsMatch(PropertyExpansionContext propExpansionContext, MessageExchange msgExchange) throws Exception {
        GroovyUtilsPro groovyUtils = createGroovyUtils(propExpansionContext);

        assert Objects.equals(groovyUtils.getXmlHolder(msgExchange.getRequestContentAsXml()).getNodeValue(WSA_MSG_ID_XPATH),
            groovyUtils.getXmlHolder(msgExchange.getResponseContentAsXml()).getNodeValue(WSA_RELATES_TO_XPATH));
    }

    public static GroovyUtilsPro createGroovyUtils(PropertyExpansionContext propExpansionContext) {
        return new GroovyUtilsPro(propExpansionContext);
    }
}
