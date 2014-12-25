import org.apache.commons.lang3.StringUtils
import org.apache.maven.artifact.Artifact
import org.apache.maven.artifact.DefaultArtifact
import org.apache.maven.artifact.handler.DefaultArtifactHandler

def localRepo = project.projectBuildingRequest.localRepository

def rabbitMqServerArtifact = new DefaultArtifact("com.rabbitmq", "rabbitmq-server-generic-unix", "3.4.2", Artifact.SCOPE_TEST, "tar.gz", StringUtils.EMPTY,
    new DefaultArtifactHandler())
def rabbitMqServerArtifactLocalRepoFile = new File(localRepo.basedir, localRepo.pathOf(rabbitMqServerArtifact))
def rabbitMqServerArtifactFileName = "${rabbitMqServerArtifact.artifactId}-${rabbitMqServerArtifact.version}.${rabbitMqServerArtifact.type}"
def rabbitMqServerArtifactUrl = "http://www.rabbitmq.com/releases/rabbitmq-server/current/${rabbitMqServerArtifactFileName}"

def testItRabbitMqDir = new File(project.properties["project.build.itDirectory"], "rabbitmq")
def testItRabbitMqServerArtifactFile = new File(testItRabbitMqDir, rabbitMqServerArtifactFileName)

testItRabbitMqDir.mkdirs()

if (!rabbitMqServerArtifactLocalRepoFile.exists()) {
    ant.get(src: rabbitMqServerArtifactUrl, dest: testItRabbitMqServerArtifactFile)
    
    "mvn install:install-file -DgroupId=${rabbitMqServerArtifact.groupId} -DartifactId=${rabbitMqServerArtifact.artifactId} \
        -Dversion=${rabbitMqServerArtifact.version} -Dpackaging=${rabbitMqServerArtifact.type} -Dfile=${testItRabbitMqServerArtifactFile} -DgeneratePom=true"
        .execute()
} else {
    ant.copy(file: rabbitMqServerArtifactLocalRepoFile, tofile: testItRabbitMqServerArtifactFile)
}

ant.untar(src: testItRabbitMqServerArtifactFile, dest: testItRabbitMqDir, compression: "gzip") {
    ant.cutdirsmapper(dirs: 1)
}

def testItRabbitMqSbinDir = new File(testItRabbitMqDir, "sbin")

ant.fileset(dir: testItRabbitMqSbinDir, includes: "rabbit*").each{
    it.file.setExecutable(true, false)
}
