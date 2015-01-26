package gov.hhs.onc.phiz.xml;

import org.apache.cxf.staxutils.W3CDOMStreamReader;

public interface DomStreamFilter {
    public void filter(W3CDOMStreamReader domStreamReader);
}
