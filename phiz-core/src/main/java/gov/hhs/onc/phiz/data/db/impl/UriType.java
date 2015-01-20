package gov.hhs.onc.phiz.data.db.impl;

import java.net.URI;
import org.springframework.stereotype.Component;

@Component("basicTypeUri")
public class UriType extends AbstractPhizVarcharType<URI> {
    private static class UriTypeDescriptor extends AbstractPhizStringTypeDescriptor<URI> {
        private final static UriTypeDescriptor INSTANCE = new UriTypeDescriptor();

        private final static long serialVersionUID = 0L;

        private UriTypeDescriptor() {
            super(URI.class, URI::create, Object::toString);
        }
    }

    private final static long serialVersionUID = 0L;

    public UriType() {
        super(URI.class, UriTypeDescriptor.INSTANCE);
    }
}
