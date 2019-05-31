package gov.hhs.onc.phiz.destination;

import gov.hhs.onc.phiz.data.db.PhizEntity;

import java.net.URI;

public interface PhizAccessControl extends PhizEntity<Integer> {

    public String getDestId();

    public void setDestId(String destId);

    public String getSourceId();

    public void setSourceId(String sourceId);

}
