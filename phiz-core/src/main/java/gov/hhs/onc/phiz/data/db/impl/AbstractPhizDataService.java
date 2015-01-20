package gov.hhs.onc.phiz.data.db.impl;

import gov.hhs.onc.phiz.data.db.PhizDao;
import gov.hhs.onc.phiz.data.db.PhizDataService;
import gov.hhs.onc.phiz.data.db.PhizEntity;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;
import org.hibernate.criterion.Criterion;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public abstract class AbstractPhizDataService<T extends Serializable, U extends PhizEntity<T>, V extends PhizDao<T, U>> extends AbstractPhizDataAccessor<T, U>
    implements PhizDataService<T, U, V> {
    protected V dao;

    protected AbstractPhizDataService(Class<T> idClass, Class<U> entityClass, V dao) {
        super(idClass, entityClass);

        this.dao = dao;
    }

    @Override
    @Transactional(readOnly = false)
    public void save(U entity) {
        this.dao.save(entity);
    }

    @Override
    public List<U> findAll() {
        return this.dao.findAll();
    }

    @Override
    public List<U> findByCriteria(Criterion ... criterions) {
        return this.dao.findByCriteria(criterions);
    }

    @Nullable
    public U findById(T id) {
        return this.dao.findById(id);
    }
}
