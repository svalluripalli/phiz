package gov.hhs.onc.phiz.data.db.impl;

import gov.hhs.onc.phiz.data.db.PhizEntity;
import gov.hhs.onc.phiz.data.db.PhizDataAccessor;
import java.io.Serializable;

public abstract class AbstractPhizDataAccessor<T extends Serializable, U extends PhizEntity<T>> implements PhizDataAccessor<T, U> {
    protected Class<T> idClass;
    protected Class<U> entityClass;

    protected AbstractPhizDataAccessor(Class<T> idClass, Class<U> entityClass) {
        this.idClass = idClass;
        this.entityClass = entityClass;
    }
}
