package gov.hhs.onc.phiz.web.ws.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.addressing.Names;

public final class PhizWsAddressingUtils {
    private PhizWsAddressingUtils() {
    }

    public static String getAddressingMessageId(Message msg) throws IOException, XMLStreamException {
        AddressingProperties msgAddrProps = ContextUtils.retrieveMAPs(msg, false, MessageUtils.isOutbound(msg), false);

        if (msgAddrProps != null) {
            return msgAddrProps.getMessageID().getValue();
        }

        BufferedInputStream msgBufferedInStream = PhizWsUtils.getMarkedInputStream(msg);

        try {
            return DOMUtils.getContent(StaxUtils.read(msgBufferedInStream).getElementsByTagNameNS(Names.WSA_NAMESPACE_NAME, Names.WSA_MESSAGEID_NAME).item(0));
        } finally {
            msgBufferedInStream.reset();
        }
    }
}
