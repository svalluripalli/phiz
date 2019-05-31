package gov.hhs.onc.phiz.destination.impl;

import gov.hhs.onc.phiz.data.db.impl.AbstractPhizDao;
import gov.hhs.onc.phiz.destination.PhizAccessControl;
import gov.hhs.onc.phiz.destination.PhizAccessControlDao;
import gov.hhs.onc.phiz.destination.PhizDestination;
import gov.hhs.onc.phiz.destination.PhizDestinationDao;
import org.springframework.stereotype.Repository;

@Repository("accessControlDaoImpl")
public class PhizAccessControlDaoImpl extends AbstractPhizDao<Integer, PhizAccessControl> implements PhizAccessControlDao {
    public PhizAccessControlDaoImpl() {
        super(Integer.class, PhizAccessControl.class, PhizAccessControlImpl.class);
    }
}
