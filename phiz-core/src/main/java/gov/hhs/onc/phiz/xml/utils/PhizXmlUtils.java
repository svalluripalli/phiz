package gov.hhs.onc.phiz.xml.utils;

import gov.hhs.onc.phiz.xml.DomStreamFilter;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.iterators.NodeListIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.staxutils.PrettyPrintXMLStreamWriter;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.staxutils.W3CDOMStreamReader;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class PhizXmlUtils {
    public static class HideContentDomStreamFilter implements DomStreamFilter {
        private String hiddenContentComment;
        private Set<QName> hideContentElemQnames;

        public HideContentDomStreamFilter(Set<QName> hideContentElemQnames) {
            this("[hidden]", hideContentElemQnames);
        }

        public HideContentDomStreamFilter(String hiddenContentComment, Set<QName> hideContentElemQnames) {
            this.hiddenContentComment = hiddenContentComment;
            this.hideContentElemQnames = hideContentElemQnames;
        }

        @Override
        public void filter(W3CDOMStreamReader domStreamReader) {
            if (!this.hideContentElemQnames.contains(domStreamReader.getName())) {
                return;
            }

            Element hideContentElem = domStreamReader.getCurrentElement();

            Stream.of(IteratorUtils.toArray(new NodeListIterator(hideContentElem.getChildNodes()), Node.class)).forEach(hideContentElem::removeChild);

            hideContentElem.appendChild(hideContentElem.getOwnerDocument().createComment(this.hiddenContentComment));
        }
    }

    public static class FilteringDomStreamReader extends W3CDOMStreamReader {
        private DomStreamFilter[] domStreamFilters;

        public FilteringDomStreamReader(Element elem, DomStreamFilter ... domStreamFilters) {
            super(elem);

            this.domStreamFilters = domStreamFilters;
        }

        public FilteringDomStreamReader(Element elem, String sysId, DomStreamFilter ... domStreamFilters) {
            super(elem, sysId);

            this.domStreamFilters = domStreamFilters;
        }

        public FilteringDomStreamReader(Document doc, DomStreamFilter ... domStreamFilters) {
            super(doc);

            this.domStreamFilters = domStreamFilters;
        }

        public FilteringDomStreamReader(DocumentFragment docFrag, DomStreamFilter ... domStreamFilters) {
            super(docFrag);

            this.domStreamFilters = domStreamFilters;
        }

        @Override
        public int next() throws XMLStreamException {
            int currentEvent = super.next();

            if ((currentEvent == START_ELEMENT) && (this.getCurrentFrame().getCurrentChild() == null)) {
                Stream.of(this.domStreamFilters).forEach((domStreamFilter) -> domStreamFilter.filter(this));
            }

            return currentEvent;
        }
    }

    public static class IgnoreWhitespaceStreamFilter implements StreamFilter {
        public final static IgnoreWhitespaceStreamFilter INSTANCE = new IgnoreWhitespaceStreamFilter();

        @Override
        public boolean accept(XMLStreamReader xmlStreamReader) {
            return !xmlStreamReader.isWhiteSpace();
        }
    }

    public static class CompositeStreamFilter implements StreamFilter {
        private StreamFilter[] streamFilters;

        public CompositeStreamFilter(StreamFilter ... streamFilters) {
            this.streamFilters = streamFilters;
        }

        @Override
        public boolean accept(XMLStreamReader xmlStreamReader) {
            for (StreamFilter streamFilter : this.streamFilters) {
                if (!streamFilter.accept(xmlStreamReader)) {
                    return false;
                }
            }

            return true;
        }
    }

    private PhizXmlUtils() {
    }

    public static Map<String, Object> mapTreeContent(Supplier<Map<String, Object>> treeContentMapSupplier, Element ... elems) {
        return mapTreeContent(treeContentMapSupplier.get(), treeContentMapSupplier, elems);
    }

    public static Map<String, Object> mapTreeContent(Map<String, Object> treeContentMap, Supplier<Map<String, Object>> treeContentMapSupplier,
        Element ... elems) {
        Stream.of(elems).forEach(
            (elem) -> {
                String elemLocalName = elem.getLocalName();
                List<Element> childElems = DomUtils.getChildElements(elem);

                treeContentMap.put(
                    elemLocalName,
                    (!childElems.isEmpty() ? mapTreeContent(treeContentMapSupplier.get(), treeContentMapSupplier,
                        childElems.toArray(new Element[childElems.size()])) : DOMUtils.getContent(elem)));
            });

        return treeContentMap;
    }

    public static Document read(InputStream inStream, StreamFilter ... streamFilters) throws XMLStreamException {
        XMLStreamReader xmlStreamReader = StaxUtils.createXMLStreamReader(inStream);

        if (streamFilters.length == 1) {
            xmlStreamReader = StaxUtils.createFilteredReader(xmlStreamReader, streamFilters[0]);
        } else if (streamFilters.length > 1) {
            xmlStreamReader = StaxUtils.createFilteredReader(xmlStreamReader, new CompositeStreamFilter(streamFilters));
        }

        return StaxUtils.read(xmlStreamReader);
    }

    public static String toString(Document doc, DomStreamFilter ... domStreamFilters) throws XMLStreamException {
        return toString(doc, -1, domStreamFilters);
    }

    public static String toString(Document doc, int indentSize, DomStreamFilter ... domStreamFilters) throws XMLStreamException {
        StringWriter strWriter = new StringWriter();
        XMLStreamWriter xmlStreamWriter = StaxUtils.createXMLStreamWriter(strWriter);

        if (indentSize > 0) {
            xmlStreamWriter = new PrettyPrintXMLStreamWriter(xmlStreamWriter, indentSize);
        }

        if (domStreamFilters.length > 0) {
            StaxUtils.copy(new FilteringDomStreamReader(doc, domStreamFilters), xmlStreamWriter);
        } else {
            StaxUtils.copy(doc, xmlStreamWriter);
        }

        xmlStreamWriter.flush();
        xmlStreamWriter.close();

        return StringUtils.trim(strWriter.toString());
    }

    @Nullable
    public static String findElementContent(Element parentElem, QName elemQname) {
        return findElementContent(parentElem, elemQname.getNamespaceURI(), elemQname.getLocalPart());
    }

    @Nullable
    public static String findElementContent(Element parentElem, String elemNsUri, String elemLocalName) {
        return DOMUtils.getContent(findElement(parentElem, elemNsUri, elemLocalName));
    }

    @Nullable
    public static Element findElement(Element parentElem, QName elemQname) {
        return findElement(parentElem, elemQname.getNamespaceURI(), elemQname.getLocalPart());
    }

    @Nullable
    public static Element findElement(Element parentElem, String elemNsUri, String elemLocalName) {
        Element childElem;

        for (Node childNode : IteratorUtils.asIterable(new NodeListIterator(parentElem.getChildNodes()))) {
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if ((Objects.equals(((childElem = ((Element) childNode))).getNamespaceURI(), elemNsUri) && childElem.getLocalName().equals(elemLocalName))
                || ((childElem = findElement(childElem, elemNsUri, elemLocalName)) != null)) {
                return childElem;
            }
        }

        return null;
    }
}
