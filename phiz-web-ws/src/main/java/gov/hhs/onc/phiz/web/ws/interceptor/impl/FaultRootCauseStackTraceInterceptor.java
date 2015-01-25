package gov.hhs.onc.phiz.web.ws.interceptor.impl;

import com.github.sebhoss.warnings.CompilerWarnings;
import gov.hhs.onc.phiz.web.ws.PhizMessageContextProperties;
import gov.hhs.onc.phiz.ws.PhizWsQnames;
import gov.hhs.onc.phiz.xml.utils.PhizXmlQnameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.Soap12FaultOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component("interceptorFaultRootCauseStackTrace")
public class FaultRootCauseStackTraceInterceptor extends AbstractPhizSoapInterceptor {
    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    public FaultRootCauseStackTraceInterceptor() {
        super(Phase.PREPARE_SEND);

        this.setBeforeClasses(Soap12FaultOutInterceptor.class);
    }

    @Override
    public void handleMessage(SoapMessage msg) throws Fault {
        // noinspection ThrowableResultOfMethodCallIgnored
        Fault fault = ((Fault) msg.getContent(Exception.class));
        Throwable faultCause;

        if (!MessageUtils.getContextualBoolean(msg, PhizMessageContextProperties.FAULT_ROOT_STACK_TRACE_ENABLED, false)
            || ((faultCause = fault.getCause()) == null)) {
            return;
        }

        Element faultDetailElem = fault.getOrCreateDetail();
        Document faultDetailDoc = faultDetailElem.getOwnerDocument();

        Element faultDetailStacktraceElem =
            faultDetailDoc.createElementNS(Fault.STACKTRACE_NAMESPACE, PhizXmlQnameUtils.toReferenceString(PhizWsQnames.CXF_FAULT_ROOT_CAUSE_STACK_TRACE));
        faultDetailStacktraceElem.appendChild(faultDetailDoc.createCDATASection((StringUtils.LF
            + StringUtils.join(ExceptionUtils.getRootCauseStackTrace(faultCause), StringUtils.LF) + StringUtils.LF)));
        faultDetailElem.appendChild(faultDetailStacktraceElem);
    }
}
