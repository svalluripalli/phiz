ant.fileset(dir: properties["project.build.cxfGeneratedSourceDirectory"], includes: "**/*.java").each() {
    def File srcFile = it.file

    if (srcFile.name ==~ /^(package\-info|IIS[a-zA-Z]*Service)\.java$/) {
        srcFile.delete()
    } else if (srcFile.name ==~ /^((JAXBContext|Object)Factory)\.java$/) {
        srcFile.write(srcFile.text.replaceFirst(~/(\npublic\s+class\s+[a-zA-Z]+\s+)/,
            "\n@SuppressWarnings({ com.github.sebhoss.warnings.CompilerWarnings.RAWTYPES, com.github.sebhoss.warnings.CompilerWarnings.UNCHECKED })\$1"))
    }
}
