package gov.hhs.onc.phiz.web.tomcat.impl;

import gov.hhs.onc.phiz.logging.impl.AtomicEventId;
import gov.hhs.onc.phiz.logging.impl.EventIdMdcConverter;
import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("valveMdc")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcValve extends ValveBase {
    private final static AtomicEventId EVENT_ID = new AtomicEventId();

    @Override
    public void invoke(Request req, Response resp) throws IOException, ServletException {
        MDC.put(EventIdMdcConverter.EVENT_ID_MDC_KEY, Long.toString(EVENT_ID.getNext()));

        try {
            this.getNext().invoke(req, resp);
        } finally {
            MDC.remove(EventIdMdcConverter.EVENT_ID_MDC_KEY);
        }
    }
}
