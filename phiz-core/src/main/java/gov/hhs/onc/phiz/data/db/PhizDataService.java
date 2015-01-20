package gov.hhs.onc.phiz.data.db;

import java.io.Serializable;

public interface PhizDataService<T extends Serializable, U extends PhizEntity<T>, V extends PhizDao<T, U>> extends PhizDataAccessor<T, U> {
}
