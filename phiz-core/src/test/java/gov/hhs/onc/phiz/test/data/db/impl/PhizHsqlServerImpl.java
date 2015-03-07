package gov.hhs.onc.phiz.test.data.db.impl;

import gov.hhs.onc.phiz.test.beans.impl.AbstractPhizServerBean;
import gov.hhs.onc.phiz.test.data.db.PhizHsqlServer;
import java.io.File;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.ResourceUtils;

public class PhizHsqlServerImpl extends AbstractPhizServerBean implements PhizHsqlServer {
    private static class PhizHsqlServerWrapper extends Server {
        {
            this.setDaemon(true);
            this.setSilent(true);
        }

        @Override
        protected void printStackTrace(Throwable throwable) {
        }

        @Override
        protected void printError(String msg) {
            LOGGER.error(msg);
        }

        @Override
        protected void print(String msg) {
            LOGGER.debug(msg);
        }
    }

    private final static String DB_PATH_FORMAT_STR = ResourceUtils.FILE_URL_PREFIX + "%s;user=%s;password=%s";

    private final static Logger LOGGER = LoggerFactory.getLogger(PhizHsqlServerImpl.class);

    private File dir;
    private String user;
    private String pass;
    private PhizHsqlServerWrapper server = new PhizHsqlServerWrapper();

    @Override
    protected void stopInternal() throws Exception {
        this.server.stop();

        if (!this.isRunning()) {
            LOGGER.info(String.format("Stopped HyperSQL server (id=%s, host=%s, port=%d).", this.server.getServerId(), this.getHost(), this.getPort()));
        } else {
            throw new FatalBeanException(String.format("Unable to stop HyperSQL server (id=%s, host=%s, port=%d).", this.server.getServerId(), this.getHost(),
                this.getPort()), this.server.getServerError());
        }
    }

    @Override
    protected void startInternal() throws Exception {
        this.server.start();

        if (this.isRunning()) {
            LOGGER.info(String.format("Started HyperSQL server (id=%s, host=%s, port=%d).", this.server.getServerId(), this.getHost(), this.getPort()));
        } else {
            throw new FatalBeanException(String.format("Unable to start HyperSQL server (id=%s, host=%s, port=%d).", this.server.getServerId(), this.getHost(),
                this.getPort()), this.server.getServerError());
        }
    }

    @Override
    public boolean isRunning() {
        return (this.server.getState() == ServerConstants.SERVER_STATE_ONLINE);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.server.setDatabasePath(0, String.format(DB_PATH_FORMAT_STR, this.dir.getPath(), this.user, this.pass));
    }

    public String getDatabaseName() {
        return this.server.getDatabaseName(0, true);
    }

    public void setDatabaseName(String dbName) {
        this.server.setDatabaseName(0, dbName);
    }

    public File getDirectory() {
        return this.dir;
    }

    public void setDirectory(File dir) {
        this.dir = dir;
    }

    @Override
    public String getHost() {
        return this.server.getAddress();
    }

    @Override
    public void setHost(String host) {
        this.server.setAddress(host);
    }

    public String getPassword() {
        return this.pass;
    }

    public void setPassword(String pass) {
        this.pass = pass;
    }

    @Override
    public int getPort() {
        return this.server.getPort();
    }

    @Override
    public void setPort(int port) {
        this.server.setPort(port);
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
