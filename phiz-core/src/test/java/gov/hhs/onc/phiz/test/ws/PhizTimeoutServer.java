package gov.hhs.onc.phiz.test.ws;

import gov.hhs.onc.phiz.test.beans.PhizHttpServer;
import javax.net.ssl.SSLEngine;

public interface PhizTimeoutServer extends PhizHttpServer {
    public SSLEngine getSslEngine();

    public void setSslEngine(SSLEngine sslEngine);
}
