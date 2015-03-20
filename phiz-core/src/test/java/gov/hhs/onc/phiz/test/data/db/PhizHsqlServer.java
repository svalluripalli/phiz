package gov.hhs.onc.phiz.test.data.db;

import gov.hhs.onc.phiz.test.beans.PhizServer;
import java.io.File;

public interface PhizHsqlServer extends PhizServer {
    public String getDatabaseName();

    public void setDatabaseName(String dbName);

    public File getDirectory();

    public void setDirectory(File dir);

    public String getPassword();

    public void setPassword(String pass);

    public String getUser();

    public void setUser(String user);
}
