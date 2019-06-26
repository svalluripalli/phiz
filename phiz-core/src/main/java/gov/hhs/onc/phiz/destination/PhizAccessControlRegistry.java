package gov.hhs.onc.phiz.destination;

import gov.hhs.onc.phiz.data.db.PhizDataService;

public interface PhizAccessControlRegistry extends PhizDataService<Integer, PhizAccessControl, PhizAccessControlDao> {

    public Boolean checkSource(String sourceId);
    public Boolean checkDest(String sourceId, String destId);

}
