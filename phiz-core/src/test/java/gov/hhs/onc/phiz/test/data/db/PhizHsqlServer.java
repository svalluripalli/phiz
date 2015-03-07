package gov.hhs.onc.phiz.test.data.db;

import gov.hhs.onc.phiz.test.beans.PhizServerBean;
import java.io.File;
import org.springframework.beans.factory.InitializingBean;

public interface PhizHsqlServer extends InitializingBean, PhizServerBean {
    public String getDatabaseName();

    public void setDatabaseName(String dbName);

    public File getDirectory();

    public void setDirectory(File dir);

    public String getPassword();

    public void setPassword(String pass);

    public String getUser();

    public void setUser(String user);
}
