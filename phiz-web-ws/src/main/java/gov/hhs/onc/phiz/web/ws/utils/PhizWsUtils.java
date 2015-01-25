package gov.hhs.onc.phiz.web.ws.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.DelegatingInputStream;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

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
    public static HttpServletResponse getHttpServletResponse(Message msg) {
        return getProperty(msg, AbstractHTTPDestination.HTTP_RESPONSE, HttpServletResponse.class);
    }

    @Nullable
    public static HttpServletResponse getHttpServletResponse(MessageContext msgContext) {
        return getProperty(msgContext, AbstractHTTPDestination.HTTP_RESPONSE, HttpServletResponse.class);
    }

    @Nullable
    public static HttpServletRequest getHttpServletRequest(Message msg) {
        return getProperty(msg, AbstractHTTPDestination.HTTP_REQUEST, HttpServletRequest.class);
    }

    @Nullable
    public static HttpServletRequest getHttpServletRequest(MessageContext msgContext) {
        return getProperty(msgContext, AbstractHTTPDestination.HTTP_REQUEST, HttpServletRequest.class);
    }

    @Nullable
    public static <T> T getProperty(Message msg, String propName, Class<T> propValueClass) {
        return getPropertyInternal(msg, propName, propValueClass);
    }

    @Nullable
    public static <T> T getProperty(MessageContext msgContext, String propName, Class<T> propValueClass) {
        return getPropertyInternal(msgContext, propName, propValueClass);
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

    @Nullable
    private static <T> T getPropertyInternal(Map<String, Object> props, String propName, Class<T> propValueClass) {
        return propValueClass.cast(props.get(propName));
    }
}
