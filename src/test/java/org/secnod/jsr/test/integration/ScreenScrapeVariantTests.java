package org.secnod.jsr.test.integration;

import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.secnod.jsr.JsrId;

@RunWith(Parameterized.class)
public class ScreenScrapeVariantTests extends IntegrationTest {

    @Parameters(name= "JSR{0} {1} {2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { 53, "servlet", "jsp" },
            { 245, "", "el" },
            { 318, "ejb", "interceptors" },
            { 220, "simplified", "persistence"},
            { 220, "simplified", "ejbcore"},
            { 220, "persistence", "ejbcore"},
        });
    }

    private JsrId jsr1;
    private JsrId jsr2;

    public ScreenScrapeVariantTests(int jsrNumber, String variant1, String variant2) {
        jsr1 = JsrId.of(jsrNumber, variant1);
        jsr2 = JsrId.of(jsrNumber, variant2);
    }

    @Test
    public void jsrVariantsDiffer() throws IOException, URISyntaxException {
        assertNotEquals(download(jsr1), download(jsr2));
    }
}
