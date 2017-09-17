package org.secnod.jsr.util;

import java.io.File;

public final class FilenameUtils {

    private FilenameUtils() {}

    public static String getExtension(String filename) {
        int split = filename.lastIndexOf('.');
        return split >= 0 ? filename.substring(split + 1) : "";
    }

    public static String getBaseName(String filename) {
        int split = filename.indexOf('.');
        return split >= 0 ? filename.substring(0, split) : "";
    }

    public static void main(String[] args) {
        System.out.println(getBaseName("baz"));

        System.out.println(getExtension("f.baz"));
    }

    // Copied from {@code org.apache.commons.io.FileNameUtils} in Apache Commons IO 2.0.1

    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * The system separator character.
     */
    private static final char SYSTEM_SEPARATOR = File.separatorChar;

    /**
     * Determines if Windows file system is in use.
     *
     * @return true if the system is Windows
     */
    static boolean isSystemWindows() {
        return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
    }
}
