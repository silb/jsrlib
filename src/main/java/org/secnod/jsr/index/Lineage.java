package org.secnod.jsr.index;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.secnod.jsr.Jsr;
import org.secnod.jsr.JsrId;

import static java.lang.String.format;

class Lineage {
    private final ArrayList<Jsr> jsrs = new ArrayList<>(); // oldest JSR first
    private final Map<JsrId, Lineage> lineages;

    Lineage(Jsr jsr, Map<JsrId, Lineage> lineages) {
        jsrs.add(jsr);
        this.lineages = lineages;
        this.lineages.put(jsr.id, this);
    }

    /**
     * Replace the heir of this lineage
     *
     * @param heir the newest JSR in this lineage
     */
    private void newHeir(Jsr heir) {
        lineages.remove(heir.succeeds);
        lineages.put(heir.id, this);
    }

    void add(Jsr jsr) {
        if (jsr.succeeds == null)
            throw new IllegalArgumentException(format("The JSR %s has no predecessor", jsr));
        for (ListIterator<Jsr> i = jsrs.listIterator(); i.hasNext();) {
            Jsr predecessor = i.next();
            if (predecessor.id.equals(jsr.succeeds)) {
                if (!jsr.specifiesPackages())
                    // Inheriting package names from earlier revision.
                    jsr.packages = packageNamesFor(predecessor);
                i.add(jsr);
                if (!i.hasNext())
                    newHeir(jsr);
                return;
            }
        }
        throw new IllegalStateException(format("JSR %s succeeds unknown JSR %s", jsr, jsr.succeeds));
    }

    IndexEntry filterByPackage(String packageName) {
        var filtered = new LinkedHashSet<Jsr>();
        for (int i = jsrs.size() - 1; i >= 0; i--) {
            Jsr jsr = jsrs.get(i);
            if (jsr.packages.contains(packageName))
                filtered.add(jsr);
        }
        return new IndexEntry(filtered);
    }

    Set<String> allPackageNames() {
        Set<String> packageNames = new HashSet<>();
        for (Jsr jsr : jsrs) {
            packageNames.addAll(jsr.packages);
        }
        return packageNames;
    }

    Jsr findJsr(JsrId jsrId) {
        for (Jsr jsr : jsrs)
            if (jsrId.equals(jsr.id))
                return jsr;
        return null;
    }

    private Set<String> packageNamesFor(Jsr jsr) {
        if (jsr.specifiesPackages()) {
            return jsr.packages;
        }

        if (jsr.succeeds != null) {
            return packageNamesFor(findJsr(jsr.succeeds));
        }

        return Set.of();
    }

    Jsr heir() {
        return jsrs.get(jsrs.size());
    }

    @Override
    public String toString() {
        return jsrs.toString();
    }
}
