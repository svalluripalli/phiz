package gov.hhs.onc.phiz.web.ws.interceptor.impl;

import gov.hhs.onc.phiz.web.ws.PhizWsHttpHeaders;
import gov.hhs.onc.phiz.web.ws.utils.PhizWsUtils;
import gov.hhs.onc.phiz.ws.iis.MessageTooLargeFault;
import gov.hhs.onc.phiz.ws.iis.SecurityFault;
import gov.hhs.onc.phiz.ws.iis.hub.DestinationConnectionFault;
import gov.hhs.onc.phiz.ws.iis.hub.HubClientFault;
import gov.hhs.onc.phiz.ws.iis.hub.HubRequestHeaderType;
import gov.hhs.onc.phiz.ws.iis.hub.UnknownDestinationFault;
import gov.hhs.onc.phiz.ws.iis.hub.impl.DestinationConnectionFaultTypeImpl;
import gov.hhs.onc.phiz.ws.iis.hub.impl.HubClientFaultTypeImpl;
import gov.hhs.onc.phiz.ws.iis.hub.impl.UnknownDestinationFaultTypeImpl;
import gov.hhs.onc.phiz.ws.iis.impl.MessageTooLargeFaultTypeImpl;
import gov.hhs.onc.phiz.ws.iis.impl.SecurityFaultTypeImpl;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.logging.NoOpFaultListener;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("interceptorIisHubDevAction")
public class IisHubDevActionInterceptor extends AbstractPhizSoapInterceptor {
    private final static FaultListener NO_OP_FAULT_LISTENER_INSTANCE = new NoOpFaultListener();

    @Value("${phiz.dest.iis.dev.id}")
    private String iisDevDestId;

    @Value("${phiz.dest.iis.dev.uri}")
    private String iisDevDestUriStr;

    public IisHubDevActionInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(SoapMessage msg) throws Fault {
        HubRequestHeaderType hubReqHeader = Optional.ofNullable(PhizWsUtils.getMessageContentPart(msg, HubRequestHeaderType.class)).orElse(null);
        String destId;

        if ((hubReqHeader == null) || !Objects.equals((destId = hubReqHeader.getDestinationId()), this.iisDevDestId)) {
            return;
        }

        Optional.ofNullable(Headers.getSetProtocolHeaders(msg).get(PhizWsHttpHeaders.EXT_IIS_HUB_DEV_ACTION_NAME)).ifPresent(
            (devActionValues) -> {
                String devActionValue = devActionValues.get(0), devActionFaultMsg = String.format("Fault for IIS Hub development action: %s", devActionValue);
                Exception devActionValueFaultCause = null;

                switch (devActionValue) {
                    case PhizWsHttpHeaders.EXT_IIS_HUB_DEV_ACTION_DEST_CONN_FAULT_VALUE:
                        devActionValueFaultCause =
                            new DestinationConnectionFault(devActionFaultMsg, new DestinationConnectionFaultTypeImpl(destId, this.iisDevDestUriStr));
                        break;

                    case PhizWsHttpHeaders.EXT_IIS_HUB_DEV_ACTION_HUB_CLIENT_FAULT_VALUE:
                        devActionValueFaultCause = new HubClientFault(devActionFaultMsg, new HubClientFaultTypeImpl(destId, this.iisDevDestUriStr));
                        break;

                    case PhizWsHttpHeaders.EXT_IIS_HUB_DEV_ACTION_MSG_TOO_LARGE_FAULT_VALUE:
                        // noinspection ConstantConditions
                        devActionValueFaultCause =
                            new MessageTooLargeFault(devActionFaultMsg, new MessageTooLargeFaultTypeImpl(BigInteger.valueOf(PhizWsUtils.getProperty(msg,
                                AbstractHTTPDestination.HTTP_REQUEST, HttpServletRequest.class).getContentLengthLong()), BigInteger.valueOf(0)));
                        break;

                    case PhizWsHttpHeaders.EXT_IIS_HUB_DEV_ACTION_SEC_FAULT_VALUE:
                        devActionValueFaultCause = new SecurityFault(devActionFaultMsg, new SecurityFaultTypeImpl());
                        break;

                    case PhizWsHttpHeaders.EXT_IIS_HUB_DEV_ACTION_UNKNOWN_DEST_FAULT_VALUE:
                        devActionValueFaultCause = new UnknownDestinationFault(devActionFaultMsg, new UnknownDestinationFaultTypeImpl(destId));
                        break;
                }

                if (devActionValueFaultCause != null) {
                    msg.setContextualProperty(FaultListener.class.getName(), NO_OP_FAULT_LISTENER_INSTANCE);

                    throw SoapFault.createFault(new Fault(devActionValueFaultCause), msg.getVersion());
                }
            });
    }
}
