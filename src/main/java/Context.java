import org.apache.commons.io.FilenameUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Simple wrapper class, to emulate the context class in some popular tools
 */
public class Context {
    public String bucketName;
    public String fileKeyName;
    public String localFileName;
    public String assumeRoleName;

    public Context(String localFileName, String bucketName, String assumeRoleName) {
        this.bucketName = bucketName;
        this.fileKeyName = FilenameUtils.getBaseName(localFileName) + "-" + now();
        this.localFileName = localFileName;
        this.assumeRoleName = assumeRoleName;
    }

    public String now() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
    }
}
