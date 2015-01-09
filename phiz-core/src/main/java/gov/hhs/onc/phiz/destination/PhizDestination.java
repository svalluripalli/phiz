package gov.hhs.onc.phiz.destination;

import java.net.URI;

public interface PhizDestination {
    public String getId();

    public void setId(String id);

    public URI getUri();

    public void setUri(URI uri);
}
