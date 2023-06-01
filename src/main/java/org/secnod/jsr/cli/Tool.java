package org.secnod.jsr.cli;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.secnod.jsr.Jsr;
import org.secnod.jsr.JsrId;
import org.secnod.jsr.JsrMetadata;
import org.secnod.jsr.JsrStatus;
import org.secnod.jsr.index.JsrIndex;
import org.secnod.jsr.screenscraper.DownloadedFile;
import org.secnod.jsr.screenscraper.JsrDownloadScreenScraper;
import org.secnod.jsr.screenscraper.JsrMetadataScreenScraper;
import org.secnod.jsr.screenscraper.JsrZipFileInspector;
import org.secnod.jsr.screenscraper.UrlFetcher;
import org.secnod.jsr.store.JsrDataStore;
import org.secnod.jsr.store.JsrMetadataStore;
import org.secnod.jsr.util.StringUtils;

public class Tool {

    private static JsrIndex index;

    static {
        try {
            index = new JsrIndex.Builder()
                    .data(JsrDataStore.loadJson())
                    .metadata(JsrMetadataStore.loadJson())
                    .build();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static void exitWithHelp(int status, String[] args) {
        PrintStream p = System.out;
        p.printf("Usage: java %s <command> <args>%n%n", Tool.class.getName());
        p.printf("Commands:%n");
        p.printf("  query <criteria> - query JSRs in JsrData.json%n");
        p.printf("      package <package or class name> - only JSRs specifying the given package%n");
        p.printf("      tag <tag> - only JSRs with a given tag%n");
        p.printf("      title <phrase> - only JSRs with a title containing the phrase%n");

        p.printf("  list <filter> - list all JSRs in JsrData.json, optionally filtered%n");
        p.printf("    <filter>:%n");
        p.printf("      jsr <JSR ID> - list a specific JSR%n");
        p.printf("        <JSR ID>: JSR number optionally followed by '-' and a variant name%n");
        p.printf("      umbrellas - list all umbrella JSRs%n");
        p.printf("      tags - list all tags%n");
        p.printf("      packages - list all packages specified by a JSR%n");

        p.printf("  download <spec> - download by screen scraping https://www.jcp.org%n");
        p.printf("    <spec>:%n");
        p.printf("      jsr <JSR ID> <target> - download a JSR, if target is not a file print the filename to stdout%n");
        p.printf("        <JSR ID>: JSR number optionally followed by '-' and a variant name%n");
        p.printf("        <target>: file or directory for storing the download%n");
        p.printf("      metadata - download and print metadata for all JSRs as JSON to stdout%n");

        p.printf("  check <check> - sanity checks%n");
        p.printf("    <check>:%n");
        p.printf("      data missing - list all JSRs with metadata in JsrMetadata.json that are missing in JsrData.json%n");
        p.printf("      metadata missing - list all JSRs with data in JsrData.json that are missing in JsrMetadata.json%n");

        p.println();
        p.println("System property for full stack traces: java -Dverbose ...");
        p.println();

        p.println("Examples:");
        p.println("  Find JSRs specifying the servlet API:");
        p.printf("    java %s query package javax.servlet%n", Tool.class.getName());
        p.println("  Download the servlet variant of JSR 53 to the current directory:");
        p.printf("    java %s download jsr 53-servlet .%n", Tool.class.getName());
        p.println();
        p.println("  List all Java EE 7 JSRs:");
        p.printf("    java %s query tag JavaEE7%n", Tool.class.getName());

        System.exit(status);
    }

    public static void main(final String[] args) throws Exception {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (verbose()) {
                    e.printStackTrace(System.err);
                    System.err.println();
                }
                if (e.getMessage() != null) {
                    System.err.printf("%s%n%n", e.getMessage());
                    System.exit(1);
                } else {
                    exitWithHelp(1, args);
                }
            }
        });

        if (args.length == 0)
            exitWithHelp(1, args);

        String commands = args.length > 1 ? args[0] + " " + args[1] : args[0];
        String[] restArgs = Arrays.copyOfRange(args, Math.min(args.length, 2), args.length);

        switch (commands) {
        case "query package":
            print(index.queryAllByPackage(Objects.requireNonNull(restArgs[0], "Missing package")));
            break;
        case "query tag":
            print(index.queryByTag(Objects.requireNonNull(restArgs[0], "Missing tag")));
            break;
        case "query title":
            String phrase = StringUtils.toString(asList(restArgs), " ");
            if (phrase.isEmpty()) throw new RuntimeException("Missing phrase");
            print(index.queryByTitle(phrase));
            break;
        case "list":
            print(index.all());
            break;
        case "list jsr":
            print(Objects.requireNonNull(JsrId.of(restArgs[0]), "Not a valid JSR ID: " + restArgs[0]));
            break;
        case "list tags":
            printTags();
            break;
        case "list packages":
            printPackages();
            break;
        case "list umbrellas":
            print(index.queryByUmbrella());
            break;
        case "download metadata":
            downloadMetadata();
            break;
        case "download jsr":
            downloadJsr(Objects.requireNonNull(JsrId.of(restArgs[0]), "Not a valid JSR ID: " + restArgs[0]),
                    restArgs.length > 1 ? new File(restArgs[1]) : null);
            break;
        case "check data":
            if (!"missing".equals(restArgs[0]))
                exitWithHelp(1, args);
            printDataMissing();
            break;
        case "check metadata":
            if (!"missing".equals(restArgs[0]))
                exitWithHelp(1, args);
            printMetadataMissing();
            break;
        case "help":
        case "--help":
        case "-h":
            exitWithHelp(0, args);
            break;
        default:
            System.err.println("Unknown command or incomplete arguments: " + args[0]);
            System.err.println();
            exitWithHelp(1, args);
        }
    }

    private static void printTags() {
        for (String tag : index.findAllTags())
            System.out.println(tag);
    }

    private static void printPackages() {
        for (String packageName : index.findAllPackages())
            System.out.println(packageName);
    }

    private static void printJsr(Jsr jsr, PrintWriter pw) {
        pw.printf("JSR %s: %s%n", jsr.id, jsr.title);
        pw.printf("  Description: %s%n", jsr.description);
        pw.printf("  Link: %s%n", jsr.detailsPage);

        if (jsr.specifiesPackages())
            pw.printf("  Packages: %s%n", StringUtils.toString(jsr.packages, " "));
        if (jsr.isTagged())
            pw.printf("  Tags: %s%n", StringUtils.toString(jsr.tags, " "));
        if (jsr.succeeds != null) {
            Jsr predecessor = index.queryById(jsr.succeeds);
            pw.printf("  Succeeds: JSR %s %s%n", predecessor.id, predecessor.title);
        }
        if (jsr.isUmbrella()) {
            pw.printf("  Components:%n");
            for (JsrId id : jsr.umbrella) {
                for (Jsr u : index.queryAllByIdOrNumber(id))
                    pw.printf("    JSR %s %s%n", u.id, u.title);
            }
        }
    }

    private static void print(Collection<Jsr> jsrs) throws IOException {
        try (PrintWriter pw = new PrintWriter(System.out)) {
            for (Jsr jsr : jsrs) {
                printJsr(jsr, pw);
                pw.println();
                pw.flush();
            }
        }
    }

    private static void print(JsrId jsrId) throws IOException {
        Collection<Jsr> jsrs = jsrId.hasVariant()
                             ? Set.of(index.queryById(jsrId))
                             : index.queryAllByIdOrNumber(jsrId);
        print(jsrs);
    }

    // No PDF, no Java packages or not applicable for this tool
    private static final Set<Integer> notMissing;
    static {
        int[] ids = {
                105,
                171,
                173,
                355,
                364,
                387,
                902,
                913,
        };
        var tmp = new HashSet<Integer>(ids.length);
        for (int id : ids)
            tmp.add(id);
        notMissing = Collections.unmodifiableSet(tmp);
    }

    private static void printDataMissing() throws IOException {
        Collection<JsrMetadata> meta = JsrMetadataStore.loadJson();
        Collection<Jsr> jsrs = JsrDataStore.loadJson();
        Set<Integer> jsrNumbers = new TreeSet<>();
        for (Jsr jsr : jsrs)
            jsrNumbers.add(jsr.getJsrNumber());
        var skipStatus = EnumSet.of(JsrStatus.WITHDRAWN, JsrStatus.REJECTED);
        try (PrintWriter pw = new PrintWriter(System.out)) {
            for (JsrMetadata m : meta) {
                if (skipStatus.contains(m.status) || notMissing.contains(m.id))
                    continue;
                if (!jsrNumbers.contains(m.id)) {
                    pw.printf("JSR %s: %s%n", m.id, m.title);
                    pw.printf("  %s%n", m.description);
                    pw.printf("  %s%n", m.detailsPage);
                    pw.println();
                    pw.flush();
                }
            }
        }
    }

    private static void printMetadataMissing() throws IOException {
        Collection<JsrMetadata> meta = JsrMetadataStore.loadJson();
        Set<Integer> jsrNumbers = new TreeSet<>();
        for (JsrMetadata jsrMetaData : meta)
            jsrNumbers.add(jsrMetaData.id);

        try (PrintWriter pw = new PrintWriter(System.out)) {
            for (Jsr jsr : JsrDataStore.loadJson()) {
                if (!jsrNumbers.contains(jsr.id.jsrNumber)) {
                    pw.println(jsr.id);
                    pw.flush();
                }
            }
        }
    }

    private static void downloadMetadata() throws IOException {
        Collection<JsrMetadata> metadata = new JsrMetadataScreenScraper()
                .query(EnumSet.of(JsrStatus.ACTIVE, JsrStatus.FINAL, JsrStatus.MAINTENANCE, JsrStatus.WITHDRAWN));
        try (Writer w = new OutputStreamWriter(System.out, "UTF-8")) {
            JsrMetadataStore.writeMetadata(metadata, w);
        }
    }

    private static void downloadJsr(JsrId jsrId, final File target) throws Exception {
        if (target != null) {
            if (target.isDirectory() && !target.canWrite()
                    || target.isFile() && !target.getParentFile().canWrite())
                throw new IOException("Cannot write to: " + target);
            if (target.isFile() && target.exists())
                throw new IOException("File already exists: " + target);
        }

        Jsr jsr = index.queryById(jsrId);
        if (jsr == null) {
            Collection<Jsr> variants = index.queryAllByIdOrNumber(jsrId);
            if (!variants.isEmpty()) {
                System.err.printf("Ambigious JSR %s, must be specified with variants: %s%n",
                        jsrId,
                        StringUtils.toString(variants, " "));
                System.exit(1);
            }
        }
        URI url = new JsrDownloadScreenScraper(jsrId).findDownload();
        if (url == null) {
            System.err.printf("%s failed to find download URL%n", jsrId);
            System.exit(1);
        }

        DownloadedFile download = new UrlFetcher(url).download();
        File specFile;
        if (download.file.getName().endsWith(".zip")) {
            JsrZipFileInspector i = new JsrZipFileInspector(download.file);
            specFile = i.copySpec();
            if (verbose()) System.out.printf("Downloaded %s and extracted %s%n", download.file, specFile);
        } else {
            specFile = download.file;
            if (verbose()) System.out.printf("Downloaded %s%n", download.file);
        }

        File targetFile;
        if (target != null) {
            targetFile = target.isDirectory()
                    ? new File(target, "JSR-" + jsrId.toString() + "-" + download.filename)
                    : target;
            if (targetFile.exists())
                throw new IOException("Cannot overwrite " + targetFile + " with downloaded file " + specFile);
            if (!specFile.renameTo(targetFile))
                throw new IOException("Failed to move " + specFile + " to " + targetFile);
        } else {
            targetFile = specFile;
        }
        if (target == null || !target.isFile())
            System.out.println(targetFile);
    }

    private static boolean verbose() {
        return System.getProperty("verbose") != null;
    }
}
