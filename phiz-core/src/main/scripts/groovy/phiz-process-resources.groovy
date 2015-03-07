import gov.hhs.onc.phiz.utils.PhizStringUtils

def resourcePropPlaceholderResolver = session.getPluginContext(mojoExecution.mojoDescriptor.pluginDescriptor, project).get("resourcePropPlaceholderResolver")

(ant.fileset(dir: dir) {
    PhizStringUtils.tokenize(includes).each{
        ant.include(name: it)
    }
    
    PhizStringUtils.tokenize(excludes).each{
        ant.exclude(name: it)
    }
}).each { it.file.write(resourcePropPlaceholderResolver.resolvePlaceholders(it.file.text)) }
