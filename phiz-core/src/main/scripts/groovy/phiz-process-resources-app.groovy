import org.apache.commons.lang3.StringUtils

ant.fileset(dir: "${project.properties["project.build.appassemblerDaemonsJswDirectory"]}/bin", erroronmissingdir: false, includes: project.artifactId).each{
    it.file.write(it.file.text
        .replaceFirst(~/\n\[\s+\-f\s+".BASEDIR"\/bin\/setenv\-\$\{APP_NAME\}\.sh\s+\]/,
            "\n[ -f \"\\\${BASEDIR}/../../../etc/default/\\\${APP_NAME}\" ] && . \"\\\${BASEDIR}/../../../etc/default/\\\${APP_NAME}\"\n\$0")
        .replaceFirst(~/(\n)#(RUN_AS_USER=)(\n)/, { it[1] + it[2] + project.artifactId + it[3] + "RUN_AS_GROUP=" + project.artifactId + it[3] })
    )
}

ant.fileset(dir: "${project.properties["project.build.appassemblerDaemonsJswDirectory"]}/conf", erroronmissingdir: false,
    includes: "wrapper-${project.artifactId}.conf").each{
    it.file.write(StringUtils.trim(it.file.text) + "\n\n#include conf/wrapper-${project.artifactId}-override.conf\n\n")
}

ant.fileset(dir: "${project.properties["project.build.appassemblerProgramsDirectory"]}/bin", erroronmissingdir: false,
    includes: "${project.artifactId}-*").each{
    it.file.write(it.file.text
        .replaceFirst(~/^(#!\/bin\/sh\n)(?:#[^\n]*\n)+/, "\$1\nAPP_NAME=\"${it.file.name}\"")
        .replaceFirst(~/\n\[\s+\-f\s+".BASEDIR"\/bin\/setenv\-\$\{APP_NAME\}\.sh\s+\]/,
            "\n[ -f \"\\\${BASEDIR}/../../../etc/default/\\\${APP_NAME}\" ] && . \"\\\${BASEDIR}/../../../etc/default/\\\${APP_NAME}\"\n\$0")
        .replaceFirst(~/(\nexec\s+".JAVACMD"\s+.JAVA_OPTS)([^\n]+)(\s+\\\n)/, { it[1] + it[2].replaceAll(~/\s+([^\s]+)/, " \\\\\n  \$1") + it[3] })
        .replaceAll(~/(\s+\-D)(app\.[a-z]+)/, "\$1phiz.\$2")
    )
}
