package gov.hhs.onc.phiz.data.db;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;
import org.hibernate.criterion.Criterion;

public interface PhizDataAccessor<T extends Serializable, U extends PhizEntity<T>> {
    public void save(U entity);

    public List<U> findAll();

    public List<U> findByCriteria(Criterion ... criterions);

    @Nullable
    public U findById(T id);
}
