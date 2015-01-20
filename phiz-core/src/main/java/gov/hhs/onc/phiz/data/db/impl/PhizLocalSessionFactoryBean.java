package gov.hhs.onc.phiz.data.db.impl;

import java.util.HashSet;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.hibernate.type.BasicType;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

public class PhizLocalSessionFactoryBean extends LocalSessionFactoryBean {
    private Set<BasicType> basicTypes = new HashSet<>();

    @Override
    protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sessionFactoryBuilder) {
        this.basicTypes.forEach(sessionFactoryBuilder::registerTypeOverride);

        return super.buildSessionFactory(sessionFactoryBuilder);
    }

    public void setBasicTypes(Set<BasicType> basicTypes) {
        this.basicTypes.clear();
        this.basicTypes.addAll(basicTypes);
    }
}
