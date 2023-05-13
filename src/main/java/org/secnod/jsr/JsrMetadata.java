package org.secnod.jsr;

import java.net.URI;

public class JsrMetadata {
    public int id;
    public String title;
    public String description;
    public JsrStatus status;
    public URI detailsPage;

    @Override
    public String toString() {
        return id + " " + title;
    }
}
