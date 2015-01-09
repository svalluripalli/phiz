package gov.hhs.onc.phiz.web.ws.interceptor.impl;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

public abstract class AbstractPhizPhaseInterceptor extends AbstractPhaseInterceptor<SoapMessage> {
    protected AbstractPhizPhaseInterceptor(String phase) {
        super(phase);
    }
}
