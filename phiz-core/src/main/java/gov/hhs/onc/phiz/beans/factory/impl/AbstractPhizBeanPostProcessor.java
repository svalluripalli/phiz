package gov.hhs.onc.phiz.beans.factory.impl;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public abstract class AbstractPhizBeanPostProcessor<T> implements BeanPostProcessor {
    protected Class<T> beanClass;

    protected AbstractPhizBeanPostProcessor(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        try {
            return (this.beanClass.isAssignableFrom(beanClass) ? this.postProcessAfterInitializationInternal(this.beanClass.cast(bean), beanName) : bean);
        } catch (Exception e) {
            throw new FatalBeanException(String.format("Unable to post process bean (name=%s, class=%s) after initialization.", beanName, beanClass.getName()),
                e);
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        try {
            return (this.beanClass.isAssignableFrom(beanClass) ? this.postProcessBeforeInitializationInternal(this.beanClass.cast(bean), beanName) : bean);
        } catch (Exception e) {
            throw new FatalBeanException(
                String.format("Unable to post process bean (name=%s, class=%s) before initialization.", beanName, beanClass.getName()), e);
        }
    }

    protected T postProcessAfterInitializationInternal(T bean, String beanName) throws Exception {
        return bean;
    }

    protected T postProcessBeforeInitializationInternal(T bean, String beanName) throws Exception {
        return bean;
    }
}
