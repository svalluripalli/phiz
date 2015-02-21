package gov.hhs.onc.phiz.aop.utils;

import com.github.sebhoss.warnings.CompilerWarnings;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.aop.target.SingletonTargetSource;

public final class PhizProxyUtils {
    @SuppressWarnings({ CompilerWarnings.SERIAL })
    public static class PhizSingletonTargetSource extends SingletonTargetSource {
        private Class<?> targetClass;

        public PhizSingletonTargetSource(Object target, Class<?> targetClass) {
            super(target);

            this.targetClass = targetClass;
        }

        @Override
        public Class<?> getTargetClass() {
            return this.targetClass;
        }
    }

    @SuppressWarnings({ CompilerWarnings.SERIAL })
    public static class PhizMethodAdvisor extends NameMatchMethodPointcutAdvisor {
        public PhizMethodAdvisor(Advice advice, Method ... methods) {
            this(advice, Stream.of(methods).map(Method::getName).toArray(String[]::new));
        }

        public PhizMethodAdvisor(Advice advice, String ... methodNames) {
            super(advice);

            this.setMappedNames(methodNames);
        }
    }

    private PhizProxyUtils() {
    }

    public static AspectJProxyFactory buildProxyFactory(Object target, Advisor ... advisors) {
        return buildProxyFactory(new SingletonTargetSource(target), advisors);
    }

    public static AspectJProxyFactory buildProxyFactory(Object target, Class<?> targetClass, Advisor ... advisors) {
        return buildProxyFactory(new PhizSingletonTargetSource(target, targetClass), advisors);
    }

    public static AspectJProxyFactory buildProxyFactory(TargetSource targetSource, Advisor ... advisors) {
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory();
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.setTargetSource(targetSource);
        proxyFactory.addAdvisors(advisors);

        return proxyFactory;
    }
}
