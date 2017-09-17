package org.secnod.jsr.test.integration;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.secnod.jsr.JsrId;
import org.secnod.jsr.screenscraper.DownloadedFile;

public abstract class IntegrationTest {

    static {
        LogConfig.configure();
    }

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    static DownloadedFile download(JsrId jsr) throws IOException, URISyntaxException {
        return ScreenScrapePdfSpecsTests.download(jsr, tempFolder.newFolder());
    }
}
