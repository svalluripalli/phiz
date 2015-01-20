package gov.hhs.onc.phiz.destination.impl;

import gov.hhs.onc.phiz.data.db.impl.AbstractPhizDataService;
import gov.hhs.onc.phiz.destination.PhizDestination;
import gov.hhs.onc.phiz.destination.PhizDestinationDao;
import gov.hhs.onc.phiz.destination.PhizDestinationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("destRegImpl")
public class PhizDestinationRegistryImpl extends AbstractPhizDataService<String, PhizDestination, PhizDestinationDao> implements PhizDestinationRegistry {
    @Autowired
    public PhizDestinationRegistryImpl(PhizDestinationDao dao) {
        super(String.class, PhizDestination.class, dao);
    }
}
