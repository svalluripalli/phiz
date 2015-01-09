package gov.hhs.onc.phiz.destination.impl;

import gov.hhs.onc.phiz.destination.PhizDestination;
import gov.hhs.onc.phiz.destination.PhizDestinationRegistry;
import java.net.URI;
import javax.annotation.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("destRegStatic")
public class StaticDestinationRegistry implements InitializingBean, PhizDestinationRegistry {
    @Value("${phiz.dest.iis.dev.id}")
    private String iisDevDestId;

    @Value("${phiz.dest.iis.dev.uri}")
    private URI iisDevDestUri;

    private PhizDestination iisDevDest;

    @Nullable
    @Override
    public PhizDestination findById(String id) {
        return (id.equals(this.iisDevDestId) ? this.iisDevDest : null);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.iisDevDest = new PhizDestinationImpl(this.iisDevDestId, this.iisDevDestUri);
    }
}
