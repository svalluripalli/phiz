import java.nio.file.Files
import java.nio.file.Paths
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.IOUtils

def debFileFileset = ant.fileset(dir: project.build.directory, erroronmissingdir: false, includes: "${project.artifactId}_*.deb")

if (debFileFileset.size() == 0) {
    return
}

def debFileArchiveInStream = null
def debFileArchiveEntry
def debDataFileName = "data.tar.gz"
def debDataFileArchiveInStream = null
def debDataFileArchiveEntry
def testDebDataDirPath = Paths.get(project.properties["project.build.testDebDataDirectory"])
def testDebDataEntryPath

try {
    debFileArchiveInStream = new ArArchiveInputStream(debFileFileset[0].file.newInputStream())
    
    while ((debFileArchiveEntry = debFileArchiveInStream.nextArEntry) != null) {
        if (debFileArchiveEntry.name == debDataFileName) {
            break
        }
    }
    
    debDataFileArchiveInStream = new TarArchiveInputStream(new GzipCompressorInputStream(new ByteArrayInputStream(debFileArchiveInStream.bytes)))
    
    while ((debDataFileArchiveEntry = debDataFileArchiveInStream.nextTarEntry) != null) {
        testDebDataEntryPath = testDebDataDirPath.resolve(debDataFileArchiveEntry.name)
        
        if (debDataFileArchiveEntry.symbolicLink) {
            Files.createSymbolicLink(testDebDataEntryPath, Paths.get(debDataFileArchiveEntry.linkName))
        } else if (debDataFileArchiveEntry.directory) {
            Files.createDirectories(testDebDataEntryPath)
        } else if (debDataFileArchiveEntry.isFile()) {
            testDebDataEntryPath.bytes = IOUtils.toByteArray(debDataFileArchiveInStream, debDataFileArchiveInStream.available())
            
            if (((debDataFileArchiveEntry.mode & 0x111) - 0xff - 1) > 0) {
                testDebDataEntryPath.toFile().setExecutable(true, false)
            }
        }
    }
} finally{
    debFileArchiveInStream?.close()
    debDataFileArchiveInStream?.close()
}

ant.mkdir(dir: "${project.build.directory}/surefire-ssl")
