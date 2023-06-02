package org.secnod.jsr.screenscraper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.secnod.jsr.JsrMetadata;
import org.secnod.jsr.JsrStatus;

/**
 * Screen scraper for retrieving metadata for all JSRs.
 * <a href="https://www.jcp.org/en/jsr/all">https://www.jcp.org/en/jsr/all
 */
public class JsrMetadataScreenScraper {

    public static URI defaultAllJsrsPage;
    public final URI allJsrsPage;

    static {
        try {
            defaultAllJsrsPage = new URI("https://www.jcp.org/en/jsr/all");
        } catch (URISyntaxException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public JsrMetadataScreenScraper(URI allJsrsPage) {
        this.allJsrsPage = allJsrsPage;
    }

    public JsrMetadataScreenScraper() {
        this(defaultAllJsrsPage);
    }

    public Collection<JsrMetadata> listAll() throws IOException {
        return query(EnumSet.allOf(JsrStatus.class));
    }

    public Collection<JsrMetadata> query(Set<JsrStatus> statusFilter) throws IOException {
        Objects.requireNonNull(statusFilter, "statusFilter cannot be null");
        List<JsrMetadata> jsrs = new ArrayList<>();
        Document jsrListPage = Jsoup.connect(allJsrsPage.toString()).get();

        Elements jsrTables = jsrListPage.select("table.listBy_table");

        for (Element jsrTable : jsrTables) {
            JsrMetadata jsr = new JsrMetadata();
            Element titleRow = jsrTable.select("tr").first();
            Element detailsPageLink = titleRow.select("td a[href~=/en/jsr/detail\\?.*]").first();

            Pattern p = Pattern.compile("^.*detail.*[\\?&]id=(\\d+).*$");
            Matcher m = p.matcher(detailsPageLink.attr("href"));
            if (!m.matches())
                continue;

            jsr.id = Integer.parseInt(m.group(1));
            jsr.title = detailsPageLink.text();
            try {
                jsr.detailsPage = new URI(detailsPageLink.absUrl("href"));
            } catch (URISyntaxException e) {}

            Element descriptionHeaderColumn = jsrTable.select("tr td:contains(Description)").first();
            if (descriptionHeaderColumn != null) {
                Element descriptionColumn = descriptionHeaderColumn.siblingElements().select(":matches(.*\\w+.*)").first();
                if (descriptionColumn != null)
                    jsr.description = descriptionColumn.text();
            }

            Element statusHeaderCell = jsrTable.select("tr td:contains(Status)").first();
            if (statusHeaderCell != null) {
                Element statusCell = statusHeaderCell.siblingElements().select(":matches(\\w)").first();
                if (statusCell != null) {
                    jsr.status = JsrStatus.parse(statusCell.text());
                }
            }

            if (!statusFilter.contains(jsr.status))
                continue;

            jsrs.add(jsr);
        }

        return jsrs;
    }
}
