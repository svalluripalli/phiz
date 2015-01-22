import gov.hhs.onc.phiz.utils.PhizStringUtils

def propPlaceholderResolver = session.getPluginContext(mojoExecution.mojoDescriptor.pluginDescriptor, project).get("propPlaceholderResolver")

(ant.fileset(dir: dir) {
    PhizStringUtils.tokenize(includes).each{
        ant.include(name: it)
    }
    
    PhizStringUtils.tokenize(excludes).each{
        ant.exclude(name: it)
    }
}).each { it.file.write(propPlaceholderResolver.resolvePlaceholders(it.file.text)) }
