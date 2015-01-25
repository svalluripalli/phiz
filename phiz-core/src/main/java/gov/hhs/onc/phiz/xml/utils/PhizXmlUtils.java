package gov.hhs.onc.phiz.xml.utils;

import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.staxutils.PrettyPrintXMLStreamWriter;
import org.apache.cxf.staxutils.StaxUtils;

public final class PhizXmlUtils {
    public static class IgnoreWhitespaceStreamFilter implements StreamFilter {
        public final static IgnoreWhitespaceStreamFilter INSTANCE = new IgnoreWhitespaceStreamFilter();

        @Override
        public boolean accept(XMLStreamReader xmlStreamReader) {
            return !xmlStreamReader.isWhiteSpace();
        }
    }

    private PhizXmlUtils() {
    }

    public static String toFormattedString(InputStream inStream, int indentSize) throws XMLStreamException {
        StringWriter writer = new StringWriter();
        PrettyPrintXMLStreamWriter xmlStreamWriter = new PrettyPrintXMLStreamWriter(StaxUtils.createXMLStreamWriter(writer), indentSize);

        StaxUtils.copy(StaxUtils.createFilteredReader(StaxUtils.createXMLStreamReader(inStream), IgnoreWhitespaceStreamFilter.INSTANCE), xmlStreamWriter);

        xmlStreamWriter.flush();
        xmlStreamWriter.close();

        return StringUtils.trim(writer.toString());
    }
}
