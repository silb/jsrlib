package org.secnod.jsr.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.secnod.jsr.Jsr;
import org.secnod.jsr.JsrId;
import org.secnod.jsr.JsrMetadata;

/**
 * A queryable index of JSRs. Built from {@code JsrData.json} and {@code JsrMetadata.json}.
 * <p>
 * A JSR can succeed an earlier JSR. If the succeeding JSR specifies the same packages as its predecessor, the
 * succeeding JSR need not specify any packages.
 * </p>
 * <p>
 * A JSR can also be an umbrella JSR of consisting of several other JSRs. Umbrella JSRs do not have any packages. They
 * can be tagged, and every tag on an umbrella JSR will be added to the JSRs it consists of.
 * </p>
 * <p>
 * JSRs are organized into {@linkplain Lineage lineages} based on the chain of successors from the most recent
 * {@linkplain Lineage#heir() heir} JSR and back to the earliest ancestor.
 * </p>
 * <p>
 * All JSRs in a lineage need not specify the same packages. When used as an {@link IndexEntry} in {@link #index} for a
 * specific package, a {@link Lineage} is {@linkplain Lineage#filterByPackage(String) turned into} one index entry for
 * each package it specifies.
 * </p>
 * <p>
 * Finally, this index maps packages names to multiple {@linkplain IndexEntry index entries}.
 * </p>
 */
public class JsrIndex {

    private final Set<Jsr> jsrs;
    private final Map<String, Collection<IndexEntry>> index;

    private JsrIndex(Collection<Jsr> jsrs, Map<String, Collection<IndexEntry>> index) {
        this.jsrs = new TreeSet<>(jsrs);
        this.index = index;
    }

    public Set<Jsr> all() {
        return new TreeSet<>(jsrs);
    }

    public Jsr queryById(JsrId id) {
        for (Jsr jsr : jsrs)
            if (id.equals(jsr.id)) return jsr;
        return null;
    }

    public Collection<Jsr> queryAllByIdOrNumber(JsrId id) {
        List<Jsr> matches = new ArrayList<>();
        List<Jsr> partialMatches = new ArrayList<>();
        for (Jsr jsr : jsrs) {
            if (id.equals(jsr.id))
                matches.add(jsr);
            else if (id.jsrNumber.equals(jsr.id.jsrNumber))
                partialMatches.add(jsr);
        }
        matches.addAll(partialMatches);
        return matches;
    }

    public Jsr queryByPackage(String packageName) {
        Collection<Jsr> jsrs = queryAllByPackage(packageName);
        return jsrs != null && !jsrs.isEmpty() ? jsrs.iterator().next() : null;
    }

    public Collection<Jsr> queryAllByPackage(String packageName) {
        while (packageName != null) {
            Collection<IndexEntry> entries = index.get(packageName);
            if (entries == null) {
                packageName = levelUp(packageName);
                continue;
            }

            List<Jsr> matches = new ArrayList<>();
            for (IndexEntry r : entries) {
                matches.addAll(r.jsrs);
            }

            if (matches.isEmpty()) {
                packageName = levelUp(packageName);
                continue;
            }

            return matches;
        }
        return List.of();
    }

    public Collection<Jsr> queryByTag(String tag) {
        Set<Jsr> matches = new TreeSet<>();
        for (Jsr jsr : jsrs)
            if (jsr.isTagged() && jsr.tags.contains(tag))
                matches.add(jsr);
        return matches;
    }

    public Collection<Jsr> queryByTitle(String phrase) {
        String searchTerm = phrase.toLowerCase();
        Set<Jsr> matches = new TreeSet<>();
        for (Jsr jsr : jsrs)
            if (jsr.title.toLowerCase().contains(searchTerm))
                matches.add(jsr);
        return matches;
    }

    public Collection<Jsr> queryByUmbrella() {
        Set<Jsr> umbrellas = new TreeSet<>();
        for (Jsr jsr : jsrs)
            if (jsr.isUmbrella())
                umbrellas.add(jsr);
        return umbrellas;

    }

    public Jsr jsrNumberForPackage(String packageName) {
        return queryByPackage(packageName);
    }

    public Collection<JsrId> jsrNumbersForPackage(String packageName) {
        List<JsrId> jsrIds = new ArrayList<>();
        Collection<Jsr> matches = queryAllByPackage(packageName);
        for (Jsr jsr : matches) {
            jsrIds.add(jsr.id);
        }
        return jsrIds;
    }

    public Collection<String> findAllTags() {
        Set<String> tags = new TreeSet<>();
        for (Jsr jsr : jsrs)
            if (jsr.isTagged())
                tags.addAll(jsr.tags);
        return tags;
    }

    public Collection<String> findAllPackages() {
        Set<String> packages = new TreeSet<>();
        for (Jsr jsr : jsrs)
            if (jsr.specifiesPackages())
                packages.addAll(jsr.packages);
        return packages;
    }

    private static String levelUp(String packageName) {
        int pos = packageName.lastIndexOf('.');
        return pos != -1 ? packageName.substring(0, pos) : null;
    }

    public static class Builder {
        private SortedMap<JsrId, Jsr> entries = new TreeMap<>(); // ordering by JSR number used as a heuristic for indexing
        private Map<String, Collection<IndexEntry>> index = new HashMap<>();
        private Collection<JsrMetadata> metadata = List.of();

        private Map<Integer, JsrMetadata> metadataIndex = new HashMap<>();

        private Map<JsrId, Lineage> lineages = new HashMap<>(); // the newest JSR of each lineage
        private Map<Integer, Set<Jsr>> variantIndex = new HashMap<>(); // variants of each JSR number
        private Set<Jsr> umbrellas = new HashSet<>();

        public Builder() {}

        public Builder data(Collection<Jsr> jsrs) {
            for (Jsr jsr : jsrs)
                entries.put(jsr.id, jsr);
            return this;
        }

        public Builder metadata(Collection<JsrMetadata> metadata) {
            if (metadata != null)
                this.metadata = metadata;
            return this;
        }

        public JsrIndex build() {
            for (Jsr jsr : entries.values())
                if (jsr.succeeds != null && !entries.containsKey(jsr.succeeds))
                    throw new IllegalStateException("JSR " + jsr + " succeeds unknown JSR " + jsr.succeeds);

            for (JsrMetadata datum : metadata)
                metadataIndex.put(datum.id, datum);

            var pendingSuccessor = new ArrayList<Jsr>();

            for (Jsr jsr : entries.values()) {
                Set<Jsr> variants = variantIndex.get(jsr.id.jsrNumber); // For tagging umbrella JSRs
                if (variants == null) {
                    variants = new HashSet<>();
                    variantIndex.put(jsr.id.jsrNumber, variants);
                }
                variants.add(jsr);

                JsrMetadata metadata = metadataIndex.get(jsr.id.jsrNumber);
                jsr.merge(metadata);

                if (jsr.isUmbrella()) {
                    umbrellas.add(jsr);
                    // Umbrella JSRs do not specify package names directly.
                    continue;
                }

                if (jsr.succeeds != null && !lineages.containsKey(jsr.succeeds)) {
                    // Handle succeeding JSR before preceding later
                    pendingSuccessor.add(jsr);
                    continue;
                }

                if (jsr.succeeds != null && lineages.containsKey(jsr.succeeds)) {
                    Lineage kin = lineages.get(jsr.succeeds);
                    kin.add(jsr);
                } else {
                    if (!jsr.isUmbrella() && !jsr.specifiesPackages())
                        throw new IllegalStateException("Missing package names for " + jsr.id);
                    lineages.put(jsr.id, new Lineage(jsr, lineages));
                }
            }

            while (!pendingSuccessor.isEmpty()) {
                for (Iterator<Jsr> i = pendingSuccessor.iterator(); i.hasNext();) {
                    Jsr jsr = i.next();
                    Lineage kin = lineages.get(jsr.succeeds);
                    if (kin == null)
                        continue;
                    kin.add(jsr);
                    i.remove();
                }
            }

            // Tag inheritance from umbrella JSRs
            for (Jsr umbrella : umbrellas) {
                for (JsrId jsrId : umbrella.umbrella) {
                    Set<Jsr> targets = variantIndex.get(jsrId.jsrNumber); // umbrells all variants of a JSR number
                    if (targets == null || targets.isEmpty())
                        throw new IllegalStateException(umbrella + " is umbrella for missing JSR " + jsrId);
                    for (Jsr jsr : targets)
                        jsr.tag(umbrella.tags);
                }
            }

            for (Lineage kin : lineages.values()) {
                for (String packageName : kin.allPackageNames()) {
                    IndexEntry indexEntry = kin.filterByPackage(packageName);
                    Collection<IndexEntry> existing = index.get(packageName);
                    if (existing != null)
                        existing.add(indexEntry);
                    else
                        index.put(packageName, new ArrayList<>(List.of(indexEntry)));
                }
            }

            return new JsrIndex(entries.values(), index);
        }
    }
}
