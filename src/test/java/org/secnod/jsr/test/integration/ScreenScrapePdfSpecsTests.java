package org.secnod.jsr.test.integration;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.secnod.jsr.JsrId;
import org.secnod.jsr.screenscraper.DownloadedFile;
import org.secnod.jsr.screenscraper.JsrDownloadScreenScraper;
import org.secnod.jsr.screenscraper.JsrZipFileInspector;
import org.secnod.jsr.screenscraper.UrlFetcher;

public class ScreenScrapePdfSpecsTests extends IntegrationTest {

    static DownloadedFile download(JsrId jsr, File dir) throws IOException, URISyntaxException {
        URI d = new JsrDownloadScreenScraper(jsr).findDownload();
        assertNotNull("Null download URL for " + jsr, d);
        return new UrlFetcher(d, dir).download();
    }

    private static void assertPdfInZip(JsrId jsr) throws IOException, URISyntaxException {
        DownloadedFile download = download(jsr);
        assertThat(download.file.getName(), endsWith(".zip"));
        JsrZipFileInspector i = new JsrZipFileInspector(download.file);
        File spec = i.copySpec(tempFolder.newFile());
        assertTrue("No PDF found for " + jsr, spec.length() > 0);
    }

    @Test
    public void jsr286PdfInZip() throws IOException, URISyntaxException {
        assertPdfInZip(JsrId.of(286));
    }

    @Test
    public void jsr298PdfInZip() throws IOException, URISyntaxException {
        assertPdfInZip(JsrId.of(298));
    }
}
