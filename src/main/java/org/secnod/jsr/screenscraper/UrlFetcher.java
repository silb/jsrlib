package org.secnod.jsr.screenscraper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.secnod.jsr.util.FilenameUtils;

/**
 * Download by following up to a limited number of redirects. Also follows redirects
 * across protocols (e.g. HTTP -> HTTPS).
 */
public class UrlFetcher {

    private final URI url;
    private File directory;
    private File file;
    private String filename;
    private int redirectLimit = 10;
    private int redirectNo = 1;
    private List<HttpURLConnection> connections = new ArrayList<HttpURLConnection>();

    public UrlFetcher(URI url, File target) {
        this.url = url;
        if (target == null) throw new IllegalArgumentException("Argument target is null");

        if (target.isDirectory()) directory = target;

        if (target.isFile()) {
            file = target;
            directory = target.getParentFile();
        }
    }

    public UrlFetcher(URI url) {
        this(url, new File(System.getProperty("java.io.tmpdir")));
    }

    private void determineTargetFile(String filename1, String filename2) throws IOException {
        String filename = filename1 != null ? filename1 : filename2;
        this.filename = filename;
        if (file != null) return;
        if (filename == null || filename.isEmpty()) {
            file = File.createTempFile("download", "", directory);
        } else {
            file = new File(directory, filename);
            if (file.exists()) {
                String prefix = FilenameUtils.getBaseName(filename);
                String extension = FilenameUtils.getExtension(filename);
                String suffix = extension.isEmpty() ? null : "." + extension;
                file = File.createTempFile(prefix, suffix, directory);
            }
        }
    }

    public DownloadedFile download() throws IOException, URISyntaxException {
        try {
            URI redirection = url;
            do {
                redirection = downloadUrl(redirection);
            } while (redirection != null);
            return new DownloadedFile(file, filename);
        } finally {
            closeUrlConnections();
        }
    }

    private void closeUrlConnections() {
        for (HttpURLConnection con : connections) {
            if (con != null) con.disconnect();
        }
    }

    private URI downloadUrl(URI url) throws IOException, URISyntaxException {
        HttpURLConnection con = (HttpURLConnection) url.toURL().openConnection();
        connections.add(con);
        con.setInstanceFollowRedirects(false);
        int status = con.getResponseCode();
        if (status == 301 || status == 302) {
            // HttpURLConnection doesn't follow redirects from http to https automatically.
            if (redirectNo > redirectLimit) throw new IOException("Redirect limit " + redirectLimit + " exceeded.");
            redirectNo++;
            String location = con.getHeaderField("Location");
            return new URI(location);
        }
        if (status != 200) throw new IOException("HTTP status " + status + " for URL " + url);
        determineTargetFile(filenameFromContentDisposition(con.getHeaderField("Content-Disposition")), filenameFromUrl(url));
        try (InputStream is = new BufferedInputStream(con.getInputStream())) {
            streamToFile(is);
        }
        return null;
    }

    private static String stripIllegalFilenameChars(String filename) {
        String basename = FilenameUtils.getBaseName(filename);
        String extension = FilenameUtils.getExtension(filename);

        StringBuilder sb = new StringBuilder();

        if (basename != null) sb.append(basename.replaceAll("[^\\w]+", ""));
        if (extension != null) sb.append('.').append(extension.replaceAll("[^\\w]+", ""));

        String trimmed = sb.toString().trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String filenameFromUrl(URI url) {
        Pattern p = Pattern.compile("/([^/\\\\]+\\.[^/\\\\]+)$");
        Matcher m = p.matcher(url.getPath());
        return m.find() ? stripIllegalFilenameChars(m.group(1)) : null;
    }

    private String filenameFromContentDisposition(String headerField) {
        if (headerField == null || headerField.isEmpty()) return null;
        if (headerField.contains("/") || headerField.contains("\\")) return null; // NO path separators allowed
        Pattern p = Pattern.compile("attachment\\s+;\\s+filename\\s+=\\s+\"(\\w+)\"");
        Matcher m = p.matcher(headerField);
        return m.find() ? stripIllegalFilenameChars(m.group(1)) : null;
    }

    private void streamToFile(InputStream is) throws IOException {
        if (file.exists() && file.length() > 0) throw new IOException("File " + file + " already exists.");
        try (OutputStream fo = new BufferedOutputStream(new FileOutputStream(file))) {
            int b = 0;
            while ((b = is.read()) != -1) fo.write(b);
            fo.flush();
        }
    }
}
