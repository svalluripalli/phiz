import gov.hhs.onc.phiz.context.PhizProperties
import groovy.xml.XmlUtil
import java.util.stream.Stream
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.config.BeanExpressionContext
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.boot.bind.PropertySourceUtils
import org.springframework.context.expression.StandardBeanExpressionResolver
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.MutablePropertySources
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySourcesPropertyResolver
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.support.PropertiesLoaderUtils
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternUtils

def pluginContextMap = session.getPluginContext(mojoExecution.mojoDescriptor.pluginDescriptor, project)

def resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader())

def beanFactory = new DefaultListableBeanFactory()
beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver())

def beanExprResolver = beanFactory.beanExpressionResolver
def beanExprContext = new BeanExpressionContext(beanFactory, null)

def propSources = new MutablePropertySources()
def propSourcesPropResolver = new PropertySourcesPropertyResolver(propSources)

propSources.addLast(new MapPropertySource(UUID.randomUUID().toString(), PropertySourceUtils.getSubProperties(new StandardEnvironment().getPropertySources(),
    StringUtils.EMPTY)));

propSources.addLast(new PropertiesPropertySource(UUID.randomUUID().toString(), project.properties))

Stream.concat(Stream.of(resourcePatternResolver.getResources("${ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX}META-INF/phiz/phiz*-test.properties")),
    Stream.of(resourcePatternResolver.getResources("${ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX}META-INF/phiz/phiz*.properties")))
    .map{ new PropertiesPropertySource(UUID.randomUUID().toString(), PropertiesLoaderUtils.loadProperties(it)) }
    .forEach{ propSources.addLast(it) }

def resolvedProps = new Properties()
pluginContextMap.put("resolvedProps", resolvedProps)

PropertySourceUtils.getSubProperties(propSources, PhizProperties.PREFIX).forEach{ propName, propValue ->
    if (!resolvedProps.containsKey((propName = (PhizProperties.PREFIX + propName)))) {
        resolvedProps.setProperty(propName, Objects.toString(beanExprResolver.evaluate(propSourcesPropResolver.resolveRequiredPlaceholders(
            Objects.toString(propValue, null)), beanExprContext), null))
    }
}

def resolvedPropSources = new MutablePropertySources()
resolvedPropSources.addLast(new PropertiesPropertySource(UUID.randomUUID().toString(), resolvedProps))
pluginContextMap.put("resolvedPropSources", resolvedPropSources)

def resolvedPropSourcesPropResolver = new PropertySourcesPropertyResolver(resolvedPropSources)
resolvedPropSourcesPropResolver.setPlaceholderPrefix("@{")
pluginContextMap.put("resolvedPropSourcesPropResolver", resolvedPropSourcesPropResolver)

(ant.fileset(dir: project.properties["project.build.itDirectory"]) {
    ant.include(name: "**/*.conf")
    ant.include(name: "**/*.config")
    ant.include(name: "**/*.json")
    ant.include(name: "**/*.properties")
    ant.include(name: "**/*.sql")
    ant.include(name: "**/*.xml")
}).each{
    def itFileContent = it.file.text
    
    if (it.file.name == "pom.xml") {
        def itProjectXmlSlurper = new XmlSlurper(false, false)
        itProjectXmlSlurper.keepIgnorableWhitespace = true
        
        def itProjectXml = itProjectXmlSlurper.parseText(itFileContent)
        def itProjectXmlDepsElem = itProjectXml.dependencies
        
        project.dependencies.each{ dep ->
            itProjectXmlDepsElem.appendNode {
                dependency {
                    groupId(dep.groupId)
                    artifactId(dep.artifactId)
                    version(dep.version)
                    
                    if (dep.type != "jar") {
                        type(dep.type)
                    }
                    
                    if (!StringUtils.isEmpty(dep.classifier)) {
                        classifier(dep.classifier)
                    }
                    
                    if (dep.scope != "compile") {
                        scope(dep.scope)
                    }
                }
            }
        }
        
        itFileContent = XmlUtil.serialize(itProjectXml)
    }
    
    it.file.write(((String) beanExprResolver.evaluate(resolvedPropSourcesPropResolver.resolveRequiredPlaceholders(itFileContent), beanExprContext)))
}
