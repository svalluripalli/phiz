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
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("interceptorIisHubDevAction")
public class IisHubDevActionInterceptor extends AbstractPhizPhaseInterceptor {
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

                switch (devActionValue) {
                    case PhizWsHttpHeaders.EXT_IIS_HUB_DEV_ACTION_DEST_CONN_FAULT_VALUE:
                        throw new SoapFault(StringUtils.EMPTY, new DestinationConnectionFault(devActionFaultMsg, new DestinationConnectionFaultTypeImpl(destId,
                            this.iisDevDestUriStr)), SoapFault.FAULT_CODE_SERVER);

                    case PhizWsHttpHeaders.EXT_IIS_HUB_DEV_ACTION_HUB_CLIENT_FAULT_VALUE:
                        throw new HubClientFault(devActionFaultMsg, new HubClientFaultTypeImpl(destId, this.iisDevDestUriStr));

                    case PhizWsHttpHeaders.EXT_IIS_HUB_DEV_ACTION_MSG_TOO_LARGE_FAULT_VALUE:
                        // noinspection ConstantConditions
                        throw new MessageTooLargeFault(devActionFaultMsg, new MessageTooLargeFaultTypeImpl(BigInteger.valueOf(PhizWsUtils
                            .getHttpServletRequest(msg).getContentLengthLong()), BigInteger.valueOf(-1)));

                    case PhizWsHttpHeaders.EXT_IIS_HUB_DEV_ACTION_SEC_FAULT_VALUE:
                        throw new SecurityFault(devActionFaultMsg, new SecurityFaultTypeImpl());

                    case PhizWsHttpHeaders.EXT_IIS_HUB_DEV_ACTION_UNKNOWN_DEST_FAULT_VALUE:
                        throw new UnknownDestinationFault(devActionFaultMsg, new UnknownDestinationFaultTypeImpl(destId));
                }
            });
    }
}
