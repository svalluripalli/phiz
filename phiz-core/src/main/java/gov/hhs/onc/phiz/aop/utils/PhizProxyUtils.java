package gov.hhs.onc.phiz.aop.utils;

import com.github.sebhoss.warnings.CompilerWarnings;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
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

    @FunctionalInterface
    public static interface PhizMethodInterceptor extends MethodInterceptor {
        @Nullable
        @Override
        default public Object invoke(MethodInvocation invocation) throws Throwable {
            Method method = invocation.getMethod();

            return this.invoke(invocation, method, method.getName(), invocation.getArguments(), invocation.getThis());
        }

        @Nullable
        public Object invoke(MethodInvocation invocation, Method method, String methodName, Object[] args, @Nullable Object target) throws Throwable;
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

    public final static String ENHANCER_CLASS_NAME_PREFIX = "$";
    public final static String ENHANCER_CLASS_NAME_SUFFIX = "$$EnhancerBySpringCGLIB$$";

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
