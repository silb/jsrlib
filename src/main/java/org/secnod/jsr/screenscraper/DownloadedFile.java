package org.secnod.jsr.screenscraper;

import java.io.File;

/**
 * A downloaded file that preserves the filename requested by the download.
 */
public class DownloadedFile {
    public final File file;
    public final String filename;

    public DownloadedFile(File file, String filename) {
        super();
        this.file = file;
        this.filename = filename != null && !filename.isEmpty() ? filename : file.getName();
    }
}
