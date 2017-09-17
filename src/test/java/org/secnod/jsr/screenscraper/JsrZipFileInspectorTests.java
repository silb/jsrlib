package org.secnod.jsr.screenscraper;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JsrZipFileInspectorTests {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void pdfInZipSelection() throws IOException {
        File zipFile = new File(getClass().getResource("/jsr-zip-two-pdfs.zip").getFile());
        File pdfFile = new JsrZipFileInspector(zipFile).copySpec(tempFolder.newFile());
        assertEquals("The largest PDF file was not chosen", 2, pdfFile.length());
    }
}
