package org.secnod.jsr.screenscraper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.secnod.jsr.util.FilenameUtils;

/**
 * Inspects a JSR bundle (zip file) and locates the specification document.
 */
public class JsrZipFileInspector {

    private final File zipFile;

    public JsrZipFileInspector(File zipFile) {
        super();
        this.zipFile = zipFile;
    }

    public File copySpec() throws IOException {
        return copySpec(null);
    }

    public File copySpec(File target) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile)) {
            List<ZipEntry> pdfDocs = new ArrayList<ZipEntry>();
            ZipEntry entry = null;
            Enumeration<? extends ZipEntry> e = zip.entries();
            while(e.hasMoreElements()) {
                entry = e.nextElement();
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".pdf")) {
                    pdfDocs.add(entry);
                }
            }

            if (pdfDocs.isEmpty()) return null;

            ZipEntry spec = Collections.max(pdfDocs, new Comparator<ZipEntry>() {
                @Override
                public int compare(ZipEntry o1, ZipEntry o2) {
                    // Select the largest PDF file.
                    return Long.compare(o1.getSize(), o2.getSize());
                }
            });

            File pdfFile = target != null
                    ? target
                    : File.createTempFile(FilenameUtils.getBaseName(spec.getName()), ".pdf");

            try (InputStream pdfSrc = zip.getInputStream(spec);
                 FileOutputStream pdfTarget = new FileOutputStream(pdfFile)) {
                int len = 0;
                byte[] buf = new byte[4096];
                while ((len = pdfSrc.read(buf)) != -1)
                    pdfTarget.write(buf, 0, len);
                return pdfFile;
            }
        }
    }
}
