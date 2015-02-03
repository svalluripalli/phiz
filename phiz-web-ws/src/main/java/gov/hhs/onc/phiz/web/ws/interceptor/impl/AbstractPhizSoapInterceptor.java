package gov.hhs.onc.phiz.web.ws.interceptor.impl;

import com.github.sebhoss.warnings.CompilerWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.lang3.ClassUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.PhaseInterceptor;

public abstract class AbstractPhizSoapInterceptor extends AbstractPhaseInterceptor<SoapMessage> {
    protected AbstractPhizSoapInterceptor(String phase) {
        super(phase);
    }

    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    public void setAfterClasses(Class<? extends PhaseInterceptor<? extends Message>> ... afterClasses) {
        this.setAfterClasses(Arrays.asList(afterClasses));
    }

    public void setAfterClasses(Collection<Class<? extends PhaseInterceptor<? extends Message>>> afterClasses) {
        this.setAfter(ClassUtils.convertClassesToClassNames(new ArrayList<>(afterClasses)));
    }

    public void setAfter(String ... afterStrs) {
        this.setAfter(Arrays.asList(afterStrs));
    }

    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    public void setBeforeClasses(Class<? extends PhaseInterceptor<? extends Message>> ... beforeClasses) {
        this.setBeforeClasses(Arrays.asList(beforeClasses));
    }

    public void setBeforeClasses(Collection<Class<? extends PhaseInterceptor<? extends Message>>> beforeClasses) {
        this.setBefore(ClassUtils.convertClassesToClassNames(new ArrayList<>(beforeClasses)));
    }

    public void setBefore(String ... beforeStrs) {
        this.setBefore(Arrays.asList(beforeStrs));
    }
}
