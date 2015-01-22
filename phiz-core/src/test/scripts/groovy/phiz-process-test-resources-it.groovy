import groovy.xml.XmlUtil
import org.apache.commons.lang3.StringUtils

def itPomXmlFile = new File(dir, "pom.xml")

if (!itPomXmlFile.exists()) {
    return
}

def itPomXmlSlurper = new XmlSlurper(false, false)
itPomXmlSlurper.keepIgnorableWhitespace = true

def itPomXml = itPomXmlSlurper.parseText(itPomXmlFile.text)
def itPomXmlDepsElem = itPomXml.dependencies

project.dependencies.each{ dep ->
    itPomXmlDepsElem.appendNode {
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

itPomXmlFile.text = XmlUtil.serialize(itPomXml)
