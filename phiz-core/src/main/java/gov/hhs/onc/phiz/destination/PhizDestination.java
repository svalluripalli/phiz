package gov.hhs.onc.phiz.destination;

import gov.hhs.onc.phiz.data.db.PhizEntity;
import java.net.URI;

public interface PhizDestination extends PhizEntity<String> {
    public URI getUri();

    public void setUri(URI uri);
}
