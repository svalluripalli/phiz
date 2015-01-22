import gov.hhs.onc.phiz.beans.factory.impl.EmbeddedPlaceholderResolverImpl
import gov.hhs.onc.phiz.beans.factory.impl.PropertiesPropertyResolver
import java.util.stream.Collectors
import java.util.stream.Stream
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.ImmutablePair
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.boot.bind.PropertySourceUtils
import org.springframework.context.expression.StandardBeanExpressionResolver
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.support.PropertiesLoaderUtils
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.format.support.FormattingConversionService
import org.springframework.util.StringValueResolver

def pluginContextMap = session.getPluginContext(mojoExecution.mojoDescriptor.pluginDescriptor, project)

def resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader())
pluginContextMap.put("resourcePatternResolver", resourcePatternResolver)

def env = new StandardEnvironment()
pluginContextMap.put("env", env)

def convService = new FormattingConversionService()
pluginContextMap.put("convService", convService)

def props = new Properties()
props.putAll(PropertySourceUtils.getSubProperties(env.getPropertySources(), StringUtils.EMPTY))
pluginContextMap.put("props", props)

Stream.concat(Stream.of(resourcePatternResolver.getResources("${ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX}META-INF/phiz/phiz*-test.properties")),
    Stream.of(resourcePatternResolver.getResources("${ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX}META-INF/phiz/phiz*.properties")))
    .flatMap{
        def loadedProps = PropertiesLoaderUtils.loadProperties(it)
        
        return loadedProps.stringPropertyNames().stream().map{ new ImmutablePair<>(it, loadedProps.getProperty(it, null)) }
    }.forEach{ props.putIfAbsent(it.key, it.value) }

def propResolver = new PropertiesPropertyResolver(props)
propResolver.conversionService = convService
propResolver.placeholderPrefix = "@{"
pluginContextMap.put("propResolver", propResolver)

def beanFactory = new DefaultListableBeanFactory()
beanFactory.addEmbeddedValueResolver(new StringValueResolver() {
    @Override
    def String resolveStringValue(String str) {
        return propResolver.resolveRequiredPlaceholders(str)
    }
})
beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver())
pluginContextMap.put("beanFactory", beanFactory)

def propPlaceholderResolver = new EmbeddedPlaceholderResolverImpl()
propPlaceholderResolver.setBeanFactory(beanFactory)
propPlaceholderResolver.afterPropertiesSet()
pluginContextMap.put("propPlaceholderResolver", propPlaceholderResolver)

def resolvedProps = new Properties()
resolvedProps.putAll(props.stringPropertyNames().stream().collect(Collectors.toMap({ it },
    { propPlaceholderResolver.resolvePlaceholders(props.getProperty(it)) })))
pluginContextMap.put("resolvedProps", resolvedProps)
