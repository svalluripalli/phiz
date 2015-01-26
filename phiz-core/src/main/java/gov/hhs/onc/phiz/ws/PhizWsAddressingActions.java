package gov.hhs.onc.phiz.ws;

import gov.hhs.onc.phiz.xml.PhizXmlNs;
import gov.hhs.onc.phiz.xml.utils.PhizXmlQnameUtils;

public final class PhizWsAddressingActions {
    public final static String IIS_PREFIX = PhizXmlNs.IIS + PhizXmlQnameUtils.REF_DELIM;
    public final static String IIS_HUB_PREFIX = PhizXmlNs.IIS_HUB + PhizXmlQnameUtils.REF_DELIM;

    public final static String SUBMIT_SINGLE_MSG_PREFIX = IIS_PREFIX + PhizWsNames.PORT_TYPE + PhizXmlQnameUtils.REF_DELIM;
    public final static String SUBMIT_SINGLE_MSG_HUB_PREFIX = IIS_HUB_PREFIX + PhizWsNames.PORT_TYPE_HUB + PhizXmlQnameUtils.REF_DELIM;

    public final static String SUBMIT_SINGLE_MSG_REQ = SUBMIT_SINGLE_MSG_PREFIX + PhizWsNames.SUBMIT_SINGLE_MSG_REQ;
    public final static String SUBMIT_SINGLE_MSG_REQ_HUB = SUBMIT_SINGLE_MSG_HUB_PREFIX + PhizWsNames.SUBMIT_SINGLE_MSG_REQ;
    public final static String SUBMIT_SINGLE_MSG_RESP = SUBMIT_SINGLE_MSG_PREFIX + PhizWsNames.SUBMIT_SINGLE_MSG_RESP;
    public final static String SUBMIT_SINGLE_MSG_RESP_HUB = SUBMIT_SINGLE_MSG_HUB_PREFIX + PhizWsNames.SUBMIT_SINGLE_MSG_RESP;

    private PhizWsAddressingActions() {
    }
}
