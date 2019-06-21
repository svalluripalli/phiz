package gov.hhs.onc.phiz.ws;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;

public final class PhizWsNames {
    public final static String HUB_PREFIX = "Hub";
    public final static String IIS_PREFIX = "IIS";
    public final static String IIS_HUB_PREFIX = IIS_PREFIX + HUB_PREFIX;

    public final static String PORT_TYPE_SUFFIX = "PortType";
    public final static String PORT_TYPE_2011_SUFFIX = "_PortType";
    public final static String BINDING_SUFFIX = "BindingSoap12";
    public final static String REQ_SUFFIX = "Request";
    public final static String RESP_SUFFIX = "Response";
    public final static String HEADER_SUFFIX = "Header";
    public final static String FAULT_SUFFIX = "Fault";
    public final static String MSG_SUFFIX = "Message";
    public final static String SERVICE_SUFFIX = "Service";
    public final static String PORT_SUFFIX = "PortSoap12";

    public final static String CXF_FAULT_ROOT_CAUSE_STACK_TRACE = "rootCause" + StringUtils.capitalize(Fault.STACKTRACE);

    public final static String USERNAME = "Username";
    public final static String PASSWORD = "Password";
    public final static String FACILITY_ID = "FacilityID";
    public final static String HL7_MSG = "Hl7" + MSG_SUFFIX;

    public final static String PORT_TYPE = IIS_PREFIX + PORT_TYPE_SUFFIX;
    public final static String PORT_TYPE_2011 = IIS_PREFIX + PORT_TYPE_2011_SUFFIX;
    public final static String PORT_TYPE_HUB = IIS_HUB_PREFIX + PORT_TYPE_SUFFIX;

    public final static String CONN_TEST_OP = "ConnectivityTest";

    public final static String CONN_TEST_REQ = CONN_TEST_OP + REQ_SUFFIX;
    public final static String CONN_TEST_REQ_MSG = CONN_TEST_REQ + MSG_SUFFIX;
    public final static String CONN_TEST_RESP = CONN_TEST_OP + RESP_SUFFIX;
    public final static String CONN_TEST_RESP_MSG = CONN_TEST_RESP + MSG_SUFFIX;

    public final static String SUBMIT_SINGLE_MSG_OP = "SubmitSingleMessage";
    public final static String SUBMIT_SINGLE_MSG_OP_2011 = "submitSingleMessage";

    public final static String SUBMIT_SINGLE_MSG_REQ = SUBMIT_SINGLE_MSG_OP + REQ_SUFFIX;
    public final static String SUBMIT_SINGLE_MSG_REQ_2011 = "submitSingleMessage";
    public final static String SUBMIT_SINGLE_MSG_REQ_MSG = SUBMIT_SINGLE_MSG_REQ + MSG_SUFFIX;
    public final static String SUBMIT_SINGLE_MSG_RESP = SUBMIT_SINGLE_MSG_OP + RESP_SUFFIX;
    public final static String SUBMIT_SINGLE_MSG_RESP_MSG = SUBMIT_SINGLE_MSG_RESP + MSG_SUFFIX;

    public final static String HUB_REQ_HEADER = HUB_PREFIX + REQ_SUFFIX + HEADER_SUFFIX;
    public final static String HUB_RESP_HEADER = HUB_PREFIX + RESP_SUFFIX + HEADER_SUFFIX;

    public final static String MSG_TOO_LARGE_FAULT = "MessageTooLarge" + FAULT_SUFFIX;
    public final static String MSG_TOO_LARGE_FAULT_MSG = MSG_TOO_LARGE_FAULT + MSG_SUFFIX;
    public final static String SEC_FAULT = "Security" + FAULT_SUFFIX;
    public final static String SEC_FAULT_MSG = SEC_FAULT + MSG_SUFFIX;
    public final static String UNSUPPORTED_OP_FAULT = "UnsupportedOperation" + FAULT_SUFFIX;
    public final static String UNSUPPORTED_OP_FAULT_MSG = UNSUPPORTED_OP_FAULT + MSG_SUFFIX;

    public final static String DEST_CONN_FAULT = "DestinationConnection" + FAULT_SUFFIX;
    public final static String DEST_CONN_FAULT_MSG = DEST_CONN_FAULT + MSG_SUFFIX;
    public final static String HUB_CLIENT_FAULT = HUB_PREFIX + "Client" + FAULT_SUFFIX;
    public final static String HUB_CLIENT_FAULT_MSG = HUB_CLIENT_FAULT + FAULT_SUFFIX;
    public final static String UNKNOWN_DEST_FAULT = "UnknownDestination" + FAULT_SUFFIX;
    public final static String UNKNOWN_DEST_FAULT_MSG = UNKNOWN_DEST_FAULT + MSG_SUFFIX;

    public final static String BINDING = IIS_PREFIX + BINDING_SUFFIX;
    public final static String BINDING_HUB = IIS_HUB_PREFIX + BINDING_SUFFIX;

    public final static String SERVICE = IIS_PREFIX + SERVICE_SUFFIX;
    public final static String SERVICE_2011 = "client_Service";
    public final static String SERVICE_HUB = IIS_HUB_PREFIX + SERVICE_SUFFIX;

    public final static String PORT = IIS_PREFIX + PORT_SUFFIX;
    public final static String PORT_2011 = "client_Port_Soap12";
    public final static String PORT_HUB = IIS_HUB_PREFIX + PORT_SUFFIX;

    private PhizWsNames() {
    }
}
