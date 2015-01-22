import gov.hhs.onc.phiz.crypto.ssl.PhizTlsVersions
import java.nio.file.Paths
import org.apache.commons.lang3.StringUtils
import org.apache.maven.artifact.Artifact
import org.apache.maven.artifact.DefaultArtifact
import org.apache.maven.artifact.handler.DefaultArtifactHandler
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.support.PropertiesLoaderUtils
import org.springframework.util.PropertyPlaceholderHelper

def pluginContextMap = session.getPluginContext(mojoExecution.mojoDescriptor.pluginDescriptor, project)
def resolvedProps = pluginContextMap.get("resolvedProps")
def localRepo = project.projectBuildingRequest.localRepository

def rabbitMqServerArtifact = new DefaultArtifact("com.rabbitmq", "rabbitmq-server-generic-unix", "3.4.2", Artifact.SCOPE_TEST, "tar.gz", StringUtils.EMPTY,
    new DefaultArtifactHandler())
def rabbitMqServerArtifactLocalRepoFile = new File(localRepo.basedir, "${localRepo.pathOf(rabbitMqServerArtifact)}.${rabbitMqServerArtifact.type}")
def rabbitMqServerArtifactFileName = "${rabbitMqServerArtifact.artifactId}-${rabbitMqServerArtifact.version}.${rabbitMqServerArtifact.type}"
def rabbitMqServerArtifactUrl = "http://www.rabbitmq.com/releases/rabbitmq-server/current/${rabbitMqServerArtifactFileName}"

def testItRabbitMqDir = new File(project.properties["project.build.itDirectory"], "rabbitmq")

if (!testItRabbitMqDir.exists()) {
    return
}

def testItRabbitMqServerArtifactFile = new File(testItRabbitMqDir, rabbitMqServerArtifactFileName)

testItRabbitMqDir.mkdirs()

if (!rabbitMqServerArtifactLocalRepoFile.exists()) {
    ant.get(src: rabbitMqServerArtifactUrl, dest: testItRabbitMqServerArtifactFile)
    
    ant.exec(executable: "mvn", failonerror: true) {
        ant.arg(value: "-q")
        ant.arg(value: "install:install-file")
        ant.arg(value: "-DgroupId=${rabbitMqServerArtifact.groupId}")
        ant.arg(value: "-DartifactId=${rabbitMqServerArtifact.artifactId}")
        ant.arg(value: "-Dversion=${rabbitMqServerArtifact.version}")
        ant.arg(value: "-Dpackaging=${rabbitMqServerArtifact.type}")
        ant.arg(value: "-DgeneratePom=true")
        ant.arg(value: "-Dfile=${testItRabbitMqServerArtifactFile}")
    }
}

ant.untar(src: rabbitMqServerArtifactLocalRepoFile, dest: testItRabbitMqDir, compression: "gzip") {
    ant.cutdirsmapper(dirs: 1)
}

def rabbitMqAdminExecArtifact = new DefaultArtifact("com.rabbitmq", "rabbitmq-admin", "3.4.2", Artifact.SCOPE_TEST, "py", StringUtils.EMPTY,
    new DefaultArtifactHandler())
def rabbitMqAdminExecArtifactLocalRepoFile = new File(localRepo.basedir, "${localRepo.pathOf(rabbitMqAdminExecArtifact)}.${rabbitMqServerArtifact.type}")
def rabbitMqAdminExecArtifactUrl = "http://hg.rabbitmq.com/rabbitmq-management/raw-file/rabbitmq_v3_4_2/bin/rabbitmqadmin"

def testItRabbitMqSbinDir = new File(testItRabbitMqDir, "sbin")
def testItRabbitMqAdminExecFile = new File(testItRabbitMqSbinDir, "rabbitmqadmin")
def testItRabbitMqControlExecFile = new File(testItRabbitMqSbinDir, "rabbitmqctl")
def testItRabbitMqServerExecFile = new File(testItRabbitMqSbinDir, "rabbitmq-server")

if (!rabbitMqAdminExecArtifactLocalRepoFile.exists()) {
    ant.get(src: rabbitMqAdminExecArtifactUrl, dest: testItRabbitMqAdminExecFile)
    
    ant.exec(executable: "mvn", failonerror: true) {
        ant.arg(value: "-q")
        ant.arg(value: "install:install-file")
        ant.arg(value: "-DgroupId=${rabbitMqAdminExecArtifact.groupId}")
        ant.arg(value: "-DartifactId=${rabbitMqAdminExecArtifact.artifactId}")
        ant.arg(value: "-Dversion=${rabbitMqAdminExecArtifact.version}")
        ant.arg(value: "-Dpackaging=${rabbitMqAdminExecArtifact.type}")
        ant.arg(value: "-DgeneratePom=true")
        ant.arg(value: "-Dfile=${testItRabbitMqAdminExecFile}")
    }
} else {
    ant.copy(src: rabbitMqAdminExecArtifactLocalRepoFile, dest: testItRabbitMqAdminExecFile)
}

ant.fileset(dir: testItRabbitMqSbinDir, includes: "rabbitmq*").each{
    it.file.setExecutable(true, false)
}

def testDebShareDataDir = new File(project.properties["project.build.testDebShareDataDirectory"])
def testItRabbitMqConfDir = Paths.get(testItRabbitMqDir.path, "etc", "rabbitmq").toFile()
def testItRabbitMqEnvProps = PropertiesLoaderUtils.loadProperties(new FileSystemResource(new File(testItRabbitMqConfDir, "rabbitmq-env.conf")))
def testItRabbitMqEnvPropPlaceholderHelper = new PropertyPlaceholderHelper(PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX,
    PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_SUFFIX)
def testItRabbitMqPidFile = new File(testItRabbitMqSbinDir, StringUtils.remove(testItRabbitMqEnvPropPlaceholderHelper.replacePlaceholders(
    testItRabbitMqEnvProps.getProperty("PID_FILE"), testItRabbitMqEnvProps), "\""))
def testItRabbitMqMgmtHttpPort = resolvedProps["phiz.rabbitmq.port.mgmt.http"]

Thread testItRabbitMqShutdownHookThread = new Thread({
    if (testItRabbitMqPidFile.exists()) {
        log.info(String.format("Shutting down integration testing RabbitMQ service (pid=%s).", StringUtils.trim(testItRabbitMqPidFile.text)))
        
        ant.exec(executable: testItRabbitMqControlExecFile, dir: testItRabbitMqSbinDir, spawn: true) {
            ant.arg(value: "stop")
            ant.arg(value: testItRabbitMqPidFile)
        }
    }
})

Runtime.runtime.addShutdownHook(testItRabbitMqShutdownHookThread)

ant.exec(executable: testItRabbitMqServerExecFile, dir: testItRabbitMqSbinDir, spawn: true) {
    ant.arg(value: "-kernel")
    ant.arg(value: "inet_dist_listen_min")
    ant.arg(value: 0)
    ant.arg(value: "-kernel")
    ant.arg(value: "inet_dist_listen_max")
    ant.arg(value: 0)
    ant.arg(value: "-rabbit")
    ant.arg(value: "log_levels")
    ant.arg(value: "[{connection, info}]")
    ant.arg(value: "-rabbit")
    ant.arg(value: "tcp_listeners")
    ant.arg(value: "[]")
    ant.arg(value: "-rabbit")
    ant.arg(value: "ssl_listeners")
    ant.arg(value: "[${resolvedProps["phiz.rabbitmq.port.amqps"]}]")
    ant.arg(value: "-rabbit")
    ant.arg(value: "ssl_options")
    ant.arg(value: """[
            {cacertfile, "${new File(testDebShareDataDir, resolvedProps["phiz.crypto.cred.ca.cert.file"])}"},
            {certfile, "${new File(testDebShareDataDir, resolvedProps["phiz.rabbitmq.crypto.server.cred.ssl.cert.file"])}"},
            {fail_if_no_peer_cert, ${true}},
            {honor_cipher_order, ${true}},
            {keyfile, "${new File(testDebShareDataDir, resolvedProps["phiz.rabbitmq.crypto.server.cred.ssl.key.private.file"])}"},
            {secure_renegotiate, ${true}},
            {verify, verify_peer},
            {versions, ['${PhizTlsVersions.TLS_1_2_NAME.toLowerCase()}']}
        ]""")
    ant.arg(value: "-rabbit")
    ant.arg(value: "ssl_cert_login_from")
    ant.arg(value: "common_name")
    ant.arg(value: "-rabbit")
    ant.arg(value: "ssl_handshake_timeout")
    ant.arg(value: 5000)
    ant.arg(value: "-rabbit")
    ant.arg(value: "auth_mechanisms")
    ant.arg(value: "['EXTERNAL']")
    ant.arg(value: "-rabbitmq_management")
    ant.arg(value: "listener")
    ant.arg(value: "[{port, ${testItRabbitMqMgmtHttpPort}}]")
}

ant.sleep(seconds: 2)

if (testItRabbitMqPidFile.exists()) {
    ant.exec(executable: testItRabbitMqControlExecFile, dir: testItRabbitMqSbinDir, failonerror: true) {
        ant.arg(value: "wait")
        ant.arg(value: testItRabbitMqPidFile)
    }
}

log.info(String.format("Integration testing RabbitMQ service started (pid=%s).", StringUtils.trim(testItRabbitMqPidFile.text)))

ant.exec(executable: testItRabbitMqAdminExecFile, dir: testItRabbitMqSbinDir, failonerror: true) {
    ant.arg(value: "-P")
    ant.arg(value: testItRabbitMqMgmtHttpPort)
    ant.arg(value: "import")
    ant.arg(value: new File(testItRabbitMqConfDir, "rabbitmq-broker-${project.artifactId}.json"))
}
