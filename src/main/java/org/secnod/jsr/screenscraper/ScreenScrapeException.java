package org.secnod.jsr.screenscraper;

import static java.lang.String.format;

public class ScreenScrapeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static String message(JsrDownloadScreenScraper screenScraper) {
        return format("details page: %s -> release page: %s -> download page: %s",
                screenScraper.detailsPageUrl,
                screenScraper.releasePageUrl,
                screenScraper.downloadPageUrl);
    }

    public ScreenScrapeException(JsrDownloadScreenScraper screenScraper) {
        super(message(screenScraper));
    }

    public ScreenScrapeException(JsrDownloadScreenScraper screenScraper, Throwable cause) {
        super(message(screenScraper), cause);
    }

}