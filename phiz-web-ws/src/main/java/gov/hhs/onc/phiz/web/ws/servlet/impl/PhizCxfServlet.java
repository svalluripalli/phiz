package gov.hhs.onc.phiz.web.ws.servlet.impl;

import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

public class PhizCxfServlet extends CXFNonSpringServlet {
    private final static long serialVersionUID = 0L;

    public PhizCxfServlet() {
        super(null, false);
    }
}
