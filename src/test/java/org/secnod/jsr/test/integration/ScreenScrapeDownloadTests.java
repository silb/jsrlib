package org.secnod.jsr.test.integration;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.secnod.jsr.JsrId;
import org.secnod.jsr.screenscraper.JsrDownloadScreenScraper;

@RunWith(Parameterized.class)
public class ScreenScrapeDownloadTests extends IntegrationTest {

    @Parameters(name= "JSR{0}{1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { 52, "" },
            { 168, "" },
            //{ 199, "" }, // ZIP archive with Javadocs, no PDF
            { 203, "" },
            { 224, "" },
            { 252, "" },
            { 311, "" },
            { 315, "" },
            { 317, "" },
            { 318, "" },
            { 341, "" },
            { 907, "" },
            { 914, "" },
            { 925, "" },
            { 370, "" }, // JAX-RS 2.1
        });
    }

    private JsrId jsrId;

    public ScreenScrapeDownloadTests(int jsrNumber, String variant) {
        super();
        jsrId = JsrId.of(jsrNumber, variant);
    }

    @Test
    public void downloadUrls() throws IOException, URISyntaxException {
        URI d = new JsrDownloadScreenScraper(jsrId).findDownload();
        if (d == null) fail(jsrId + ": failed to find download URL");
    }

}
