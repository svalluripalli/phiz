package gov.hhs.onc.phiz.destination;

import gov.hhs.onc.phiz.data.db.PhizEntity;
import java.net.URI;

public interface PhizDestination extends PhizEntity<String> {
    public URI getUri();

    public void setUri(URI uri);

    public String getUsername();

    public void setUsername(String username);

    public String getPassword();

    public void setPassword(String password);

    public String getVersion();

    public void setVersion(String destVersion);
}
