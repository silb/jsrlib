package org.secnod.jsr.index;

import java.util.Collections;
import java.util.Set;

import org.secnod.jsr.Jsr;

class IndexEntry {

    final Set<Jsr> jsrs;

    /**
     * @param jsrs
     *            JSRs for this entry. The order of the JSRs must be from the
     *            most recent JSR at the first position to the earliest JSR at
     *            the last position.
     */
    IndexEntry(Set<Jsr> jsrs) {
        this.jsrs = Collections.unmodifiableSet(jsrs);
    }

}
