package ains.utils;

import java.io.InputStream;
import java.util.Properties;

/**
 * Git commit the running jar was built from, written by the Gradle task generateBuildInfo.
 * A "-dirty" suffix means the working tree had uncommitted changes at build time.
 */
public final class BuildInfo {

    private static final String COMMIT = load();

    private BuildInfo() {
    }

    public static String commit() {
        return COMMIT;
    }

    private static String load() {
        try (InputStream in = BuildInfo.class.getResourceAsStream("/ains/build-info.properties")) {
            if (in == null) return "unknown";
            Properties p = new Properties();
            p.load(in);
            return p.getProperty("commit", "unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }
}
