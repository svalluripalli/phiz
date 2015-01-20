package gov.hhs.onc.phiz.test.impl;

import java.io.File;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ResourceUtils;

public class PhizHsqlServer extends Server implements InitializingBean {
    private final static String DB_PATH_FORMAT_STR = ResourceUtils.FILE_URL_PREFIX + "%s;user=%s;password=%s";

    private final static Logger LOGGER = LoggerFactory.getLogger(PhizHsqlServer.class);

    private File dir;
    private String user;
    private String pass;

    public boolean isRunning() {
        return (this.getState() == ServerConstants.SERVER_STATE_ONLINE);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.setDatabasePath(0, String.format(DB_PATH_FORMAT_STR, this.dir.getPath(), this.user, this.pass));
    }

    @Override
    protected void printStackTrace(Throwable throwable) {
        LOGGER.error(String.format("HyperSQL server (id=%s) error stack trace:", this.serverId), throwable);
    }

    @Override
    protected void printError(String msg) {
        LOGGER.error(msg);
    }

    @Override
    protected void print(String msg) {
        LOGGER.trace(msg);
    }

    public String getDatabaseName() {
        return this.getDatabaseName(0, true);
    }

    public void setDatabaseName(String dbName) {
        this.setDatabaseName(0, dbName);
    }

    public File getDirectory() {
        return this.dir;
    }

    public void setDirectory(File dir) {
        this.dir = dir;
    }

    public String getPassword() {
        return this.pass;
    }

    public void setPassword(String pass) {
        this.pass = pass;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
