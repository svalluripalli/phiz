package gov.hhs.onc.phiz.destination.impl;

import gov.hhs.onc.phiz.data.db.impl.AbstractPhizDataService;
import gov.hhs.onc.phiz.destination.*;
import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("accessControlRegImpl")
public class PhizAccessControlRegistryImpl extends AbstractPhizDataService<Integer, PhizAccessControl, PhizAccessControlDao> implements PhizAccessControlRegistry {

    @Value("phiz.ws.iis.hub.dest.check")
    private boolean enableDestCheck;

    @Autowired
    public PhizAccessControlRegistryImpl(PhizAccessControlDao dao) {
        super(Integer.class, PhizAccessControl.class, dao);
    }

    @Override
    public Boolean checkDest(String sourceId, String destId) {
        if(enableDestCheck) {
            List<PhizAccessControl> list = dao.findByQuery("FROM accessControl where accessControl.sourceId = '" + sourceId + "'"
                    + " and accessControl.destId = '" + destId + "'");

            if (list.size() > 0) {
                return true;
            }
            return false;
        }
        return true;
    }

}
