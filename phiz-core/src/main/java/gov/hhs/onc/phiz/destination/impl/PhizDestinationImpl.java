package gov.hhs.onc.phiz.destination.impl;

import gov.hhs.onc.phiz.destination.PhizDestination;
import java.net.URI;

public class PhizDestinationImpl implements PhizDestination {
    private String id;
    private URI uri;

    public PhizDestinationImpl() {
    }

    public PhizDestinationImpl(String id, URI uri) {
        this.id = id;
        this.uri = uri;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

    @Override
    public void setUri(URI uri) {
        this.uri = uri;
    }
}
