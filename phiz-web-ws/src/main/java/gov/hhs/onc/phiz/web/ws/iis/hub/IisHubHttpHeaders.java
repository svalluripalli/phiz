package gov.hhs.onc.phiz.web.ws.iis.hub;

import gov.hhs.onc.phiz.ws.PhizWsNames;

public final class IisHubHttpHeaders {
    public final static String PREFIX = "X-IIS-Hub-";
    public final static String DEV_PREFIX = PREFIX + "Dev-";

    public final static String DEV_ACTION_NAME = DEV_PREFIX + "Action";

    public final static String DEV_ACTION_MSG_TOO_LARGE_FAULT_VALUE = PhizWsNames.MSG_TOO_LARGE_FAULT;
    public final static String DEV_ACTION_SEC_FAULT_VALUE = PhizWsNames.SEC_FAULT;
    public final static String DEV_ACTION_DEST_CONN_FAULT_VALUE = PhizWsNames.DEST_CONN_FAULT;
    public final static String DEV_ACTION_HUB_CLIENT_FAULT_VALUE = PhizWsNames.HUB_CLIENT_FAULT;
    public final static String DEV_ACTION_UNKNOWN_DEST_FAULT_VALUE = PhizWsNames.UNKNOWN_DEST_FAULT;

    private IisHubHttpHeaders() {
    }
}
