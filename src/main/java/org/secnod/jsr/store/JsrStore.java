package org.secnod.jsr.store;

import java.io.File;
import java.io.IOException;

import org.secnod.jsr.JsrId;
import org.secnod.jsr.util.FileUtils;
import org.secnod.jsr.util.FilenameUtils;

public class JsrStore {

    private File directory;

    public JsrStore(File directory) throws IOException {
        super();
        if (!directory.exists() && !directory.mkdirs())
            throw new IOException("Could not create directory " + directory);
        this.directory = directory;
    }

    public File add(JsrId jsrId, File file) throws IOException {
        if (!"pdf".equalsIgnoreCase(FilenameUtils.getExtension(file.getName())))
            throw new IOException("File " + file + " for " + jsrId + " is not a PDF file.");
        File target = fileFor(jsrId);
        if (!file.renameTo(target)) throw new IOException("Could not rename + " + file + " to " + target);
        return target;
    }

    public File find(JsrId jsrId) {
        File jsrFile = fileFor(jsrId);
        return jsrFile.exists() ? jsrFile : null;
    }

    public File getDirectory() {
        return directory;
    }

    public File fileFor(JsrId jsr) {
        String filename = jsr.jsrNumber + (jsr.variant != null ? "_" + jsr.variant : "") + ".pdf";
        return new File(directory, filename);
    }

    public void purge() throws IOException {
        FileUtils.cleanDirectory(directory);
    }
}
