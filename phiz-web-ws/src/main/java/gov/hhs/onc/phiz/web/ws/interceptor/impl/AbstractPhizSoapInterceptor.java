package gov.hhs.onc.phiz.web.ws.interceptor.impl;

import com.github.sebhoss.warnings.CompilerWarnings;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        this.setAfterClasses(Stream.of(afterClasses));
    }

    public void setAfterClasses(Collection<Class<? extends PhaseInterceptor<? extends Message>>> afterClasses) {
        this.setAfterClasses(afterClasses.stream());
    }

    public void setAfterClasses(Stream<Class<? extends PhaseInterceptor<? extends Message>>> afterClasses) {
        this.setAfter(afterClasses.map(Object::getClass).map(Class::getName).collect(Collectors.toList()));
    }

    @SuppressWarnings({ CompilerWarnings.UNCHECKED })
    public void setBeforeClasses(Class<? extends PhaseInterceptor<? extends Message>> ... beforeClasses) {
        this.setBeforeClasses(Stream.of(beforeClasses));
    }

    public void setBeforeClasses(Collection<Class<? extends PhaseInterceptor<? extends Message>>> beforeClasses) {
        this.setBeforeClasses(beforeClasses.stream());
    }

    public void setBeforeClasses(Stream<Class<? extends PhaseInterceptor<? extends Message>>> beforeClasses) {
        this.setBefore(beforeClasses.map(Object::getClass).map(Class::getName).collect(Collectors.toList()));
    }
}
