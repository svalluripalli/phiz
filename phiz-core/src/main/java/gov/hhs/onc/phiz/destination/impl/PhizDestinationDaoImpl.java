package gov.hhs.onc.phiz.destination.impl;

import gov.hhs.onc.phiz.data.db.impl.AbstractPhizDao;
import gov.hhs.onc.phiz.destination.PhizDestination;
import gov.hhs.onc.phiz.destination.PhizDestinationDao;
import org.springframework.stereotype.Repository;

@Repository("destDaoImpl")
public class PhizDestinationDaoImpl extends AbstractPhizDao<String, PhizDestination> implements PhizDestinationDao {
    public PhizDestinationDaoImpl() {
        super(String.class, PhizDestination.class, PhizDestinationImpl.class);
    }
}
