package gov.hhs.onc.phiz.ws;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;

public final class PhizWsNames {
    private final static String HUB_PREFIX = "Hub";
    private final static String IIS_PREFIX = "IIS";
    private final static String IIS_HUB_PREFIX = IIS_PREFIX + HUB_PREFIX;

    private final static String PORT_TYPE_SUFFIX = "PortType";
    private final static String BINDING_SUFFIX = "BindingSoap12";
    private final static String REQ_SUFFIX = "Request";
    private final static String RESP_SUFFIX = "Response";
    private final static String HEADER_SUFFIX = "Header";
    private final static String FAULT_SUFFIX = "Fault";
    private final static String MSG_SUFFIX = "Message";
    private final static String SERVICE_SUFFIX = "Service";
    private final static String PORT_SUFFIX = "PortSoap12";

    public final static String CXF_FAULT_ROOT_STACK_TRACE = "root" + StringUtils.capitalize(Fault.STACKTRACE);

    public final static String PORT_TYPE = IIS_PREFIX + PORT_TYPE_SUFFIX;
    public final static String PORT_TYPE_HUB = IIS_HUB_PREFIX + PORT_TYPE_SUFFIX;

    public final static String CONN_TEST_OP = "ConnectivityTest";

    public final static String CONN_TEST_REQ = CONN_TEST_OP + REQ_SUFFIX;
    public final static String CONN_TEST_REQ_MSG = CONN_TEST_REQ + MSG_SUFFIX;
    public final static String CONN_TEST_RESP = CONN_TEST_OP + RESP_SUFFIX;
    public final static String CONN_TEST_RESP_MSG = CONN_TEST_RESP + MSG_SUFFIX;

    public final static String SUBMIT_SINGLE_MSG_OP = "SubmitSingleMessage";

    public final static String SUBMIT_SINGLE_MSG_REQ = SUBMIT_SINGLE_MSG_OP + REQ_SUFFIX;
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
    public final static String SERVICE_HUB = IIS_HUB_PREFIX + SERVICE_SUFFIX;

    public final static String PORT = IIS_PREFIX + PORT_SUFFIX;
    public final static String PORT_HUB = IIS_HUB_PREFIX + PORT_SUFFIX;

    private PhizWsNames() {
    }
}
