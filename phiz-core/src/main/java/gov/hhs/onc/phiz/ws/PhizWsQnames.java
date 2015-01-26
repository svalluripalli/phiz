package gov.hhs.onc.phiz.ws;

import gov.hhs.onc.phiz.xml.PhizXmlNs;
import javax.xml.namespace.QName;
import org.apache.cxf.interceptor.Fault;

public final class PhizWsQnames {
    public final static QName CXF_FAULT_ROOT_CAUSE_STACK_TRACE = new QName(Fault.STACKTRACE_NAMESPACE, PhizWsNames.CXF_FAULT_ROOT_CAUSE_STACK_TRACE,
        PhizXmlNs.CXF_FAULT_PREFIX);

    public final static QName USERNAME = new QName(PhizXmlNs.IIS, PhizWsNames.USERNAME, PhizXmlNs.IIS_PREFIX);
    public final static QName PASSWORD = new QName(PhizXmlNs.IIS, PhizWsNames.PASSWORD, PhizXmlNs.IIS_PREFIX);
    public final static QName FACILITY_ID = new QName(PhizXmlNs.IIS, PhizWsNames.FACILITY_ID, PhizXmlNs.IIS_PREFIX);
    public final static QName HL7_MSG = new QName(PhizXmlNs.IIS, PhizWsNames.HL7_MSG, PhizXmlNs.IIS_PREFIX);

    public final static QName PORT_TYPE = new QName(PhizXmlNs.IIS, PhizWsNames.PORT_TYPE, PhizXmlNs.IIS_PREFIX);
    public final static QName PORT_TYPE_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.PORT_TYPE_HUB, PhizXmlNs.IIS_HUB_PREFIX);

    public final static QName CONN_TEST_OP = new QName(PhizXmlNs.IIS, PhizWsNames.CONN_TEST_OP, PhizXmlNs.IIS_PREFIX);
    public final static QName CONN_TEST_OP_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.CONN_TEST_OP, PhizXmlNs.IIS_HUB_PREFIX);

    public final static QName CONN_TEST_REQ = new QName(PhizXmlNs.IIS, PhizWsNames.CONN_TEST_REQ, PhizXmlNs.IIS_PREFIX);
    public final static QName CONN_TEST_REQ_MSG = new QName(PhizXmlNs.IIS, PhizWsNames.CONN_TEST_REQ_MSG, PhizXmlNs.IIS_PREFIX);
    public final static QName CONN_TEST_REQ_MSG_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.CONN_TEST_REQ_MSG, PhizXmlNs.IIS_HUB_PREFIX);
    public final static QName CONN_TEST_RESP = new QName(PhizXmlNs.IIS, PhizWsNames.CONN_TEST_RESP, PhizXmlNs.IIS_PREFIX);
    public final static QName CONN_TEST_RESP_MSG = new QName(PhizXmlNs.IIS, PhizWsNames.CONN_TEST_RESP_MSG, PhizXmlNs.IIS_PREFIX);
    public final static QName CONN_TEST_RESP_MSG_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.CONN_TEST_RESP_MSG, PhizXmlNs.IIS_HUB_PREFIX);

    public final static QName SUBMIT_SINGLE_MSG_OP = new QName(PhizXmlNs.IIS, PhizWsNames.SUBMIT_SINGLE_MSG_OP, PhizXmlNs.IIS_PREFIX);
    public final static QName SUBMIT_SINGLE_MSG_OP_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.SUBMIT_SINGLE_MSG_OP, PhizXmlNs.IIS_HUB_PREFIX);

    public final static QName SUBMIT_SINGLE_MSG_REQ = new QName(PhizXmlNs.IIS, PhizWsNames.SUBMIT_SINGLE_MSG_REQ, PhizXmlNs.IIS_PREFIX);
    public final static QName SUBMIT_SINGLE_MSG_REQ_MSG = new QName(PhizXmlNs.IIS, PhizWsNames.SUBMIT_SINGLE_MSG_REQ_MSG, PhizXmlNs.IIS_PREFIX);
    public final static QName SUBMIT_SINGLE_MSG_REQ_MSG_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.SUBMIT_SINGLE_MSG_REQ_MSG, PhizXmlNs.IIS_HUB_PREFIX);
    public final static QName SUBMIT_SINGLE_MSG_RESP = new QName(PhizXmlNs.IIS, PhizWsNames.SUBMIT_SINGLE_MSG_RESP, PhizXmlNs.IIS_PREFIX);
    public final static QName SUBMIT_SINGLE_MSG_RESP_MSG = new QName(PhizXmlNs.IIS, PhizWsNames.SUBMIT_SINGLE_MSG_RESP_MSG, PhizXmlNs.IIS_PREFIX);
    public final static QName SUBMIT_SINGLE_MSG_RESP_MSG_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.SUBMIT_SINGLE_MSG_RESP_MSG, PhizXmlNs.IIS_HUB_PREFIX);

    public final static QName HUB_REQ_HEADER = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.HUB_REQ_HEADER, PhizXmlNs.IIS_HUB_PREFIX);
    public final static QName HUB_RESP_HEADER = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.HUB_RESP_HEADER, PhizXmlNs.IIS_HUB_PREFIX);

    public final static QName MSG_TOO_LARGE_FAULT = new QName(PhizXmlNs.IIS, PhizWsNames.MSG_TOO_LARGE_FAULT, PhizXmlNs.IIS_PREFIX);
    public final static QName MSG_TOO_LARGE_FAULT_MSG = new QName(PhizXmlNs.IIS, PhizWsNames.MSG_TOO_LARGE_FAULT_MSG, PhizXmlNs.IIS_PREFIX);
    public final static QName MSG_TOO_LARGE_FAULT_MSG_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.MSG_TOO_LARGE_FAULT_MSG, PhizXmlNs.IIS_HUB_PREFIX);
    public final static QName SEC_FAULT = new QName(PhizXmlNs.IIS, PhizWsNames.SEC_FAULT, PhizXmlNs.IIS_PREFIX);
    public final static QName SEC_FAULT_MSG = new QName(PhizXmlNs.IIS, PhizWsNames.SEC_FAULT_MSG, PhizXmlNs.IIS_PREFIX);
    public final static QName SEC_FAULT_MSG_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.SEC_FAULT_MSG, PhizXmlNs.IIS_HUB_PREFIX);
    public final static QName UNSUPPORTED_OP_FAULT = new QName(PhizXmlNs.IIS, PhizWsNames.UNSUPPORTED_OP_FAULT, PhizXmlNs.IIS_PREFIX);
    public final static QName UNSUPPORTED_OP_FAULT_MSG = new QName(PhizXmlNs.IIS, PhizWsNames.UNSUPPORTED_OP_FAULT_MSG, PhizXmlNs.IIS_PREFIX);
    public final static QName UNSUPPORTED_OP_FAULT_MSG_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.UNSUPPORTED_OP_FAULT_MSG, PhizXmlNs.IIS_HUB_PREFIX);

    public final static QName DEST_CONN_FAULT = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.DEST_CONN_FAULT, PhizXmlNs.IIS_HUB_PREFIX);
    public final static QName DEST_CONN_FAULT_MSG_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.DEST_CONN_FAULT_MSG, PhizXmlNs.IIS_HUB_PREFIX);
    public final static QName HUB_CLIENT_FAULT = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.HUB_CLIENT_FAULT, PhizXmlNs.IIS_HUB_PREFIX);
    public final static QName HUB_CLIENT_FAULT_MSG_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.HUB_CLIENT_FAULT_MSG, PhizXmlNs.IIS_HUB_PREFIX);
    public final static QName UNKNOWN_DEST_FAULT = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.UNKNOWN_DEST_FAULT, PhizXmlNs.IIS_HUB_PREFIX);
    public final static QName UNKNOWN_DEST_FAULT_MSG_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.UNKNOWN_DEST_FAULT_MSG, PhizXmlNs.IIS_HUB_PREFIX);

    public final static QName BINDING = new QName(PhizXmlNs.IIS, PhizWsNames.BINDING, PhizXmlNs.IIS_PREFIX);
    public final static QName BINDING_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.BINDING_HUB, PhizXmlNs.IIS_HUB_PREFIX);

    public final static QName SERVICE = new QName(PhizXmlNs.IIS, PhizWsNames.SERVICE, PhizXmlNs.IIS_PREFIX);
    public final static QName SERVICE_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.SERVICE_HUB, PhizXmlNs.IIS_HUB_PREFIX);

    public final static QName PORT = new QName(PhizXmlNs.IIS, PhizWsNames.PORT, PhizXmlNs.IIS_PREFIX);
    public final static QName PORT_HUB = new QName(PhizXmlNs.IIS_HUB, PhizWsNames.PORT_HUB, PhizXmlNs.IIS_HUB_PREFIX);

    private PhizWsQnames() {
    }
}
