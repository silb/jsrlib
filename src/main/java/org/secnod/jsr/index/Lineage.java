package org.secnod.jsr.index;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.secnod.jsr.Jsr;
import org.secnod.jsr.JsrId;

class Lineage {
    final SortedSet<Jsr> jsrs;

    Lineage() {
        this.jsrs =  new TreeSet<>();
    }

    Lineage(Collection<Jsr> jsrs) {
        this.jsrs = new TreeSet<>(jsrs);
    }

    IndexEntry filterByPackage(String packageName) {
        TreeSet<Jsr> filtered = new TreeSet<>();
        for (Iterator<Jsr> i = jsrs.iterator(); i.hasNext();) {
            Jsr jsr = i.next();
            if (jsr.packages.contains(packageName))
                filtered.add(jsr);
        }
        //return new Lineage(filtered);
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

    Set<String> packageNamesFor(JsrId jsrId) {
        Jsr jsr = findJsr(jsrId);
        if (jsr.specifiesPackages()) {
            return jsr.packages;
        }

        if (jsr.succeeds != null) {
            return packageNamesFor(jsr.succeeds);
        }

        return Collections.emptySet();
    }

    Jsr heir() {
        return jsrs.last();
    }

    @Override
    public String toString() {
        return jsrs.toString();
    }
}