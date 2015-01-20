package gov.hhs.onc.phiz.data.db;

import java.io.Serializable;

public interface PhizDao<T extends Serializable, U extends PhizEntity<T>> extends PhizDataAccessor<T, U> {
}
