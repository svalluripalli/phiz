package gov.hhs.onc.phiz.web.ws.feature.impl;

import com.github.sebhoss.warnings.CompilerWarnings;
import gov.hhs.onc.phiz.web.PhizHttpRequestMethods;
import gov.hhs.onc.phiz.web.ws.interceptor.impl.AbstractPhizSoapInterceptor;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsUtils;
import gov.hhs.onc.phiz.xml.utils.PhizXmlUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.stream.XMLStreamException;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhizLoggingFeature extends AbstractFeature {
    private abstract class AbstractPhizLoggingInterceptor extends AbstractPhizSoapInterceptor {
        protected AbstractPhizLoggingInterceptor(String phase) {
            super(phase);
        }

        @Override
        public void handleMessage(SoapMessage msg) throws Fault {
            Exchange msgExchange = msg.getExchange();

            if (!msgExchange.containsKey(LOG_MSG_ID_MSG_PROP_NAME)) {
                msgExchange.put(LOG_MSG_ID_MSG_PROP_NAME, LOG_MSG_ID.incrementAndGet());
            }

            try {
                this.handleMessageInternal(msg, ((int) msgExchange.get(LOG_MSG_ID_MSG_PROP_NAME)), new StringBuilder());
            } catch (Fault e) {
                throw e;
            } catch (Exception e) {
                throw new Fault(e);
            }
        }

        protected abstract void handleMessageInternal(SoapMessage msg, int logMsgId, StringBuilder msgBuilder) throws Exception;
    }

    private class PhizLoggingInInterceptor extends AbstractPhizLoggingInterceptor {
        public PhizLoggingInInterceptor() {
            super(Phase.RECEIVE);
        }

        @Override
        public void handleMessage(SoapMessage msg) throws Fault {
            if (!Objects.equals(msg.get(Message.HTTP_REQUEST_METHOD), PhizHttpRequestMethods.POST) || msg.containsKey(LOG_MSG_ID_MSG_PROP_NAME)) {
                return;
            }

            super.handleMessage(msg);
        }

        @Override
        protected void handleMessageInternal(SoapMessage msg, int logMsgId, StringBuilder msgBuilder) throws Exception {
            msg.put(LOG_MSG_ID_MSG_PROP_NAME, logMsgId);

            String msgReqUrl = ((String) msg.get(Message.REQUEST_URL));
            msg.getExchange().put(Message.REQUEST_URL, msgReqUrl);

            msgBuilder.append("Inbound message (logId=");
            msgBuilder.append(logMsgId);
            msgBuilder.append(", addr=");
            msgBuilder.append(msgReqUrl);
            msgBuilder.append(", headers=");
            msgBuilder.append(msg.get(Message.PROTOCOL_HEADERS));
            msgBuilder.append("):\n");

            PhizLoggingFeature.this.logMessage(msgBuilder, PhizWsUtils.getCachedInputStream(msg));
        }
    }

    private class PhizLoggingOutCallback implements CachedOutputStreamCallback {
        private SoapMessage msg;
        private StringBuilder msgBuilder;

        public PhizLoggingOutCallback(SoapMessage msg, StringBuilder msgBuilder) {
            this.msg = msg;
            this.msgBuilder = msgBuilder;
        }

        @Override
        public void onClose(CachedOutputStream msgPayloadOutStream) {
            this.msgBuilder.append(", headers=");
            this.msgBuilder.append(this.msg.get(Message.PROTOCOL_HEADERS));
            this.msgBuilder.append(", status=");
            this.msgBuilder.append(this.msg.get(Message.RESPONSE_CODE));
            this.msgBuilder.append("):\n");

            try (InputStream msgPayloadInStream = msgPayloadOutStream.getInputStream()) {
                PhizLoggingFeature.this.logMessage(this.msgBuilder, msgPayloadInStream);
            } catch (IOException | XMLStreamException ignored) {
            }
        }

        @Override
        public void onFlush(CachedOutputStream msgPayloadOutStream) {
        }
    }

    private class PhizLoggingOutInterceptor extends AbstractPhizLoggingInterceptor {
        @SuppressWarnings({ CompilerWarnings.UNCHECKED })
        public PhizLoggingOutInterceptor() {
            super(Phase.PRE_STREAM);

            this.setBeforeClasses(StaxOutInterceptor.class);
        }

        @Override
        protected void handleMessageInternal(SoapMessage msg, int logMsgId, StringBuilder msgBuilder) throws Exception {
            msgBuilder.append("Outbound message (logId=");
            msgBuilder.append(logMsgId);
            msgBuilder.append(", addr=");
            msgBuilder.append(msg.getExchange().get(Message.REQUEST_URL));

            CacheAndWriteOutputStream msgPayloadOutStream = new CacheAndWriteOutputStream(msg.getContent(OutputStream.class));
            msgPayloadOutStream.registerCallback(new PhizLoggingOutCallback(msg, msgBuilder));
            msg.setContent(OutputStream.class, msgPayloadOutStream);
        }
    }

    private final static String LOG_MSG_ID_MSG_PROP_NAME = "log.msg.id";
    private final static AtomicInteger LOG_MSG_ID = new AtomicInteger();

    private final static Logger LOGGER = LoggerFactory.getLogger(PhizLoggingFeature.class);

    private int indentSize;

    @Override
    protected void initializeProvider(InterceptorProvider interceptorProv, Bus bus) {
        PhizLoggingInInterceptor loggingInInterceptor = new PhizLoggingInInterceptor();
        interceptorProv.getInInterceptors().add(loggingInInterceptor);
        interceptorProv.getInInterceptors().add(loggingInInterceptor);

        PhizLoggingOutInterceptor loggingOutInterceptor = new PhizLoggingOutInterceptor();
        interceptorProv.getOutInterceptors().add(loggingOutInterceptor);
        interceptorProv.getOutFaultInterceptors().add(loggingOutInterceptor);
    }

    private void logMessage(StringBuilder msgBuilder, InputStream msgPayloadInStream) throws XMLStreamException {
        msgBuilder.append(PhizXmlUtils.toFormattedString(msgPayloadInStream, this.indentSize));

        LOGGER.info(msgBuilder.toString());
    }

    public int getIndentSize() {
        return this.indentSize;
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }
}
