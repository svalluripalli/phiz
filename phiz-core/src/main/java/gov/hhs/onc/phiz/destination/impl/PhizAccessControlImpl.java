package gov.hhs.onc.phiz.destination.impl;

import gov.hhs.onc.phiz.data.db.impl.AbstractPhizEntity;
import gov.hhs.onc.phiz.destination.PhizAccessControl;
import gov.hhs.onc.phiz.destination.PhizDestination;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.net.URI;

@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Cacheable
@Entity(name = "accessControl")
@Table(name = "ACCESS_CONTROL")
public class PhizAccessControlImpl extends AbstractPhizEntity<Integer> implements PhizAccessControl {
    private String destId;
    private String sourceId;

    public PhizAccessControlImpl() {
    }

    public PhizAccessControlImpl(Integer id, String sourceId, String destId) {
        this.id = id;
        this.destId = destId;
        this.sourceId = sourceId;
    }

    @Column(name = "id", nullable = false)
    @Id
    @Override
    public Integer getId() {
        return super.getId();
    }

    @Column(name = "dest_id", nullable = false)
    @Override
    public String getDestId() {
        return this.destId;
    }

    @Override
    public void setDestId(String destId) {
        this.destId = destId;
    }

    @Column(name = "src_id", nullable = false)
    @Override
    public String getSourceId() {
        return this.sourceId;
    }

    @Override
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

}
