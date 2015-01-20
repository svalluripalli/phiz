package gov.hhs.onc.phiz.data.db.impl;

import gov.hhs.onc.phiz.data.db.PhizEntity;
import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import org.hibernate.annotations.Proxy;

@Access(AccessType.PROPERTY)
@MappedSuperclass
@Proxy(lazy = false)
public abstract class AbstractPhizEntity<T extends Serializable> implements PhizEntity<T> {
    protected T id;

    @Override
    @Transient
    public T getId() {
        return this.id;
    }

    @Override
    public void setId(T id) {
        this.id = id;
    }
}
