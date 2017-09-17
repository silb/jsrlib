package org.secnod.jsr.screenscraper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.secnod.jsr.JsrId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Screen scrapes http://www.jcp.org and downloads JSRs.
 */
public class JsrDownloadScreenScraper {

    private static final Logger log = LoggerFactory.getLogger(JsrDownloadScreenScraper.class);

    String detailsPageUrl;
    String releasePageUrl;
    String downloadPageUrl;
    final JsrId jsrId;


    private Document detailsPage;
    private Document releasePage;
    private Queue<String> releases = new LinkedList<>();

    public JsrDownloadScreenScraper(JsrId jsrId) {
        super();
        this.jsrId = jsrId;
        detailsPageUrl = "http://www.jcp.org/en/jsr/detail?id=" + jsrId.jsrNumber;
    }

    public JsrDownloadScreenScraper openDetailsPage() throws IOException {
        log.debug("{} fetching details page {}", jsrId, detailsPageUrl);
        detailsPage = Jsoup.connect(detailsPageUrl).get();

        // Collecting release names which could lead to a download for the JSR

        // Locating the revision table by its expected TD content
        Elements cells = detailsPage.select("td:matchesOwn((?i)(final|maintenance)\\s+release)");

        // Trying the most deeply nested TD first. Needed for JSR 370 which contains nested tables.
        Collections.sort(cells, new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                return Integer.compare(o2.parents().size(), o1.parents().size());
            }
        });

        Element revisionTable = null;

        for (Element element : cells.parents()) {
            if ("table".equals(element.tagName())) {
                revisionTable = element;
                break;
            }
        }

        if (revisionTable == null) throw new ScreenScrapeException(this);

        Elements rows = revisionTable.select("tr");

        for (Element row : rows) {
            Elements columns = row.select("td");
            Element releaseStatusCell = columns.select("td:matches((Active|Final|Maintenance)\\s+Release)").first();
            if (releaseStatusCell == null) continue;
            String releaseName = releaseStatusCell.text();
            if (releases.contains(releaseName)) continue;
            releases.add(releaseName);
        }

        log.trace("{} releases: {}", jsrId, releases);

        return this;
    }

    public void openReleasePage() throws IOException {
        if (releases.isEmpty()) throw new ScreenScrapeException(this);
        log.debug("{} trying release {}", jsrId, releaseName());

        // Locating a cell in the table of revisions.
        Elements cell = detailsPage.select("td:matchesOwn((?i)(final|maintenance)\\s+release)");

        Element revisionTable = null;

        for (Element element : cell.parents()) {
            if ("table".equals(element.tagName())) {
                revisionTable = element;
                break;
            }
        }

        if (revisionTable == null) throw new ScreenScrapeException(this);

        Elements rows = revisionTable.select("tr");

        for (Element row : rows) {
            Elements columns = row.select("td");
            Element releaseStatusCell = columns.select("td:containsOwn(" + releaseName() + ")").first(); // FIXME Does not match!
            if (releaseStatusCell == null) continue;
            Element releaseDownloadLink = columns.select("a:containsOwn(download)").first();
            if (releaseDownloadLink == null) continue;
            releasePageUrl = releaseDownloadLink.attr("href");
            log.debug("{} {} page {}", jsrId, releaseName(), releasePageUrl);
            releasePage = Jsoup.connect(releasePageUrl).get();
            if (releasePage != null) break;
        }
        //throw new ScreenScrapeException(this);
    }

    public JsrDownloadScreenScraper openDownloadPage() throws IOException {
        if (releasePage == null) throw new ScreenScrapeException(this);

        Element downloadPageLink = releasePage.select("a[href~=.*download.*eval.*]").first();
        if (downloadPageLink == null) {
            Elements downloadPageLinks = releasePage.select("a[href~=.*download.*jcp.*]");
            if (downloadPageLinks.size() == 1) {
                // Match for JSR 914 at https://www.jcp.org/aboutJava/communityprocess/final/jsr914/index.html
                // No match for JSR 53 at https://www.jcp.org/aboutJava/communityprocess/final/jsr053/index.html
                downloadPageLink = downloadPageLinks.first();
            }
        }
        if (downloadPageLink != null) {
            // Release page like JSR 245 Final Release with links to a single download page for all variants.
            downloadPageUrl = downloadPageLink.attr("href");
        } else {
            // Release page like JSR 53 Final Release with links to different download pages for each variant.
            String anchorSelector = jsrId.variant == null ? "a" : "a[href~=-" + jsrId.variant + "-]";
            Element downloadPageImage = releasePage.select(anchorSelector + " img[alt*=download]").first();
            if (downloadPageImage != null) {
                for (Element element : downloadPageImage.parents()) {
                    if ("a".equals(element.tagName())) {
                        downloadPageUrl = element.attr("href");
                        break;
                    }
                }
            }
        }

        /*
        if (downloadPageUrl == null) {
            // Fallback to the first download link
            Element downloadPageLink = releasePage.select("a[href~=http(s)?://download.*").first();
            if (downloadPageLink != null) {
                downloadPageUrl = downloadPageLink.attr("href");
            }
        }
        */

        if (downloadPageUrl != null)
            log.debug("{} {} download page {}", jsrId, releaseName(), downloadPageUrl);
        else
            log.debug("{} {} download page not found", jsrId, releaseName());

        return this;
    }

    private String releaseName() {
        return releases.peek();
    }

    public URI findReleaseDownload() throws IOException, ScreenScrapeException {
        if (downloadPageUrl == null) return null;

        Document downloadPage = Jsoup.connect(downloadPageUrl).get();

        Elements scripts = downloadPage.select("script");

        // Try to find the full download link in the script. preferring PDF documents over zip files.
        Pattern p = Pattern.compile("http(s)?://download.oracle.com/.*\\.(pdf|zip)", Pattern.CASE_INSENSITIVE);
        for (Element script : scripts) {
            Matcher m = p.matcher(script.data());
            String downloadUrl = null;
            while(m.find() && (downloadUrl == null || !downloadUrl.toLowerCase().endsWith(".pdf"))) {
                String url = m.group();
                if (jsrId.variant == null || url.contains("-" + jsrId.variant)) {
                    downloadUrl = url;
                }
            }

            if (downloadUrl == null) continue;
            //if (jsrId.variant != null && !downloadUrl.contains("-" + jsrId.variant)) continue;

            log.trace("{} {} found a complete download URL in a script", jsrId, releaseName());
            log.debug("{} {} download URL {}", jsrId, releaseName(), downloadUrl);
            return success(newUri(downloadUrl));
        }

        // Try to assemble the download link from the script and href.

        Element downloadLink = downloadPage.select("a:matchesOwn((?i)\\.pdf|\\.zip)").first();

        if (downloadLink != null) {
            log.trace("{} {} possible download link {}", jsrId, releaseName(), downloadLink);
            String uuid = downloadLink.attr("id");
            //Pattern uuidPattern = Pattern.compile("var\\s+" + uuid + "\\s+=\\s+'(/.*\\.(pdf|zip))'", Pattern.CASE_INSENSITIVE);
            Pattern uuidPattern = Pattern.compile("var\\s+" + uuid + "\\s+=\\s+'((https?:/)?/.*\\.(pdf|zip))'", Pattern.CASE_INSENSITIVE);
            for (Element script : scripts) {
                Matcher m = uuidPattern.matcher(script.data());
                if (!m.find()) continue;
                String urlPath = m.group(1);
                if (jsrId.variant != null
                        && !(urlPath.contains("-" + jsrId.variant) || urlPath.contains(jsrId.variant + "-"))) continue;
                log.trace("{} {} found absolute or relative URL {} in script variable {} at {}",
                        jsrId, releaseName(), urlPath, uuid, downloadPageUrl);
                URI baseURI = newUri(downloadLink.baseUri());
                URI downloadUrl = baseURI.resolve(urlPath);
                log.debug("{} {} download URL {}", jsrId, releaseName(), downloadUrl);
                return success(downloadUrl);
            }
        }

        log.debug("{} {} download URL not found", jsrId, releaseName());
        return null;
        //throw new ScreenScrapeException(this);
    }

    private URI success(URI downloadUrl) {
        releases.clear();
        return downloadUrl;
    }

    private URI newUri(String spec) {
        try {
            return new URI(spec);
        } catch (URISyntaxException e) {
            throw new ScreenScrapeException(this, e);
        }
    }

    public URI findDownload() throws IOException {
        openDetailsPage();
        do {
            openReleasePage();
            openDownloadPage();
            URI download = findReleaseDownload();
            if (download != null) return download;
        } while (nextRelease());
        return null;
    }

    public boolean nextRelease() {
        releases.poll();
        return !releases.isEmpty();
    }
}

