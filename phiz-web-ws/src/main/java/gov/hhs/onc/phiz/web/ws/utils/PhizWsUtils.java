package gov.hhs.onc.phiz.web.ws.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.DelegatingInputStream;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;

public final class PhizWsUtils {
    private PhizWsUtils() {
    }

    public static InputStream getCachedInputStream(Message msg) throws IOException {
        InputStream msgInStream = msg.getContent(InputStream.class);
        DelegatingInputStream delegatingMsgInStream = ((msgInStream instanceof DelegatingInputStream) ? ((DelegatingInputStream) msgInStream) : null);
        CachedOutputStream cachedMsgOutStream = new CachedOutputStream();
        byte[] msgContent;

        IOUtils.copy(((delegatingMsgInStream != null) ? delegatingMsgInStream.getInputStream() : msgInStream), cachedMsgOutStream);

        msgInStream.close();
        cachedMsgOutStream.flush();

        msgContent = IOUtils.toByteArray(cachedMsgOutStream.getInputStream());
        msgInStream = new ByteArrayInputStream(msgContent);

        if (delegatingMsgInStream != null) {
            delegatingMsgInStream.setInputStream(msgInStream);
        } else {
            msg.setContent(InputStream.class, msgInStream);
        }

        cachedMsgOutStream.close();

        return new ByteArrayInputStream(msgContent);
    }

    @Nullable
    @SuppressWarnings({ "unchecked" })
    public static <T> T getMessageContentPart(Message msg, Class<T> msgContentPartClass) {
        return msgContentPartClass.cast(getMessageContentPart(msg, (msgContentPart) -> msgContentPartClass
            .isAssignableFrom(((msgContentPart instanceof Holder<?>) ? ((Holder<?>) msgContentPart) : msgContentPart).getClass())));
    }

    @Nullable
    public static Object getMessageContentPart(Message msg, Predicate<? super Object> msgContentPartPredicate) {
        return getMessageContents(msg).stream().filter(msgContentPartPredicate).findFirst().orElse(null);
    }

    public static MessageContentsList getMessageContents(Message msg) {
        MessageContentsList msgContents = MessageContentsList.getContentsList(msg);

        if (msgContents == null) {
            msg.setContent(List.class, (msgContents = new MessageContentsList()));
        }

        return msgContents;
    }

    @Nullable
    public static <T> T getContextualProperty(Message msg, String propName, Class<T> propValueClass) {
        return (hasContextualProperty(msg, propName) ? propValueClass.cast(msg.getContextualProperty(propName)) : null);
    }

    @Nullable
    public static String getContextualProperty(Message msg, String propName) {
        return Objects.toString(msg.getContextualProperty(propName), null);
    }

    public static boolean hasContextualProperty(Message msg, String propName) {
        return msg.getContextualPropertyKeys().contains(propName);
    }

    @Nullable
    public static <T> T getProperty(Map<String, Object> props, String propName, Class<T> propValueClass) {
        return propValueClass.cast(props.get(propName));
    }

    @Nullable
    public static String getProperty(Map<String, Object> props, String propName) {
        return Objects.toString(props.get(propName), null);
    }

    public static SoapMessage getSoapMessage(WebServiceContext wsContext) {
        return getMessage(wsContext, SoapMessage.class);
    }

    public static <T extends Message> T getMessage(WebServiceContext wsContext, Class<T> msgClass) {
        return msgClass.cast(getMessageContext(wsContext).getWrappedMessage());
    }

    public static WrappedMessageContext getMessageContext(WebServiceContext wsContext) {
        return ((WrappedMessageContext) wsContext.getMessageContext());
    }
}
