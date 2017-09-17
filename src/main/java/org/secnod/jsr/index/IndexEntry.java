package org.secnod.jsr.index;

import java.util.Collections;
import java.util.Set;

import org.secnod.jsr.Jsr;

class IndexEntry {

    final Set<Jsr> jsrs;

    IndexEntry(Set<Jsr> jsrs) {
        this.jsrs = Collections.unmodifiableSet(jsrs);
    }

}
