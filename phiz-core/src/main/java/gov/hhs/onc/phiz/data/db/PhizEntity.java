package gov.hhs.onc.phiz.data.db;

import java.io.Serializable;

public interface PhizEntity<T extends Serializable> {
    public T getId();

    public void setId(T id);
}
