import groovy.xml.XmlUtil
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.config.BeanExpressionContext
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.expression.StandardBeanExpressionResolver

def beanFactory = new DefaultListableBeanFactory()
beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver())

def beanExprResolver = beanFactory.beanExpressionResolver
def beanExprContext = new BeanExpressionContext(beanFactory, null)

(ant.fileset(dir: project.properties["project.build.itDirectory"]) {
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
    
    it.file.write(((String) beanExprResolver.evaluate(itFileContent, beanExprContext)))
}
