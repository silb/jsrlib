package org.secnod.jsr;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

public class Jsr implements Comparable<Jsr> {
    public JsrId id;
    public String title;
    public String description;
    public JsrId succeeds;
    public Set<String> packages;
    public JsrStatus status;
    public URI detailsPage;

    public Set<JsrId> umbrella;
    public Set<String> tags;

    Jsr() {}

    public Jsr(int jsrNumber) {
        id = JsrId.of(jsrNumber);
    }

    public Integer getJsrNumber() {
        return id.jsrNumber;
    }

    public String getVariant() {
        return id.variant;
    }

    public boolean specifiesPackages() {
        return packages != null && !packages.isEmpty();
    }

    public void tag(Set<String> tags) {
        if (this.tags == null)
            this.tags = new LinkedHashSet<>();
        this.tags.addAll(tags);
    }

    public boolean isTagged() {
        return tags != null && !tags.isEmpty();
    }

    public boolean isUmbrella() {
        return umbrella != null && !umbrella.isEmpty();
    }

    public void merge(JsrMetadata metadata) {
        if (metadata == null)
            return;
        if (title == null)
            title = metadata.title;
        description = metadata.description;
        status = metadata.status;
        detailsPage = metadata.detailsPage;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Jsr))
            return false;
        return id.equals(((Jsr) obj).id);
    }

    @Override
    public int compareTo(Jsr o) {
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}