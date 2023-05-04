package org.secnod.jsr.index;

import static org.secnod.jsr.index.InMemoryJsrIndexTest.asCollection;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.secnod.jsr.Jsr;
import org.secnod.jsr.JsrId;
import org.secnod.jsr.store.JsrDataStore;
import org.secnod.jsr.store.JsrMetadataStore;

public class DiskIndexTest {

    private static JsrIndex idx;

    @BeforeClass
    public static void loadIndex() throws IOException {
        idx = new JsrIndex.Builder()
                .data(JsrDataStore.loadJson())
                .metadata(JsrMetadataStore.loadJson())
                .build();
    }

    private Collection<JsrId> toIds(Collection<Jsr> jsrs) {
        List<JsrId> ids = new ArrayList<>();
        for (Jsr jsr : jsrs) {
            ids.add(jsr.id);
        }
        return ids;
    }

    @Test
    public void servletJsrs() {
        assertThat(
                toIds(idx.queryAllByPackage("javax.el")),
                equalTo(asCollection(JsrId.of(341), JsrId.of(245, "el"))));

        Collection<JsrId> servletJsrIds = asList(
                JsrId.of(369),
                JsrId.of(340),
                JsrId.of(315),
                JsrId.of(154),
                JsrId.of(53, "servlet"));

        assertThat(
                toIds(idx.queryAllByPackage("javax.servlet")),
                equalTo(servletJsrIds));

        assertThat(
                toIds(idx.queryAllByPackage("javax.servlet.http")),
                equalTo(servletJsrIds));
    }

    @Test
    public void jaxRsJsrs() {
        Collection<JsrId> jaxRsJsrIds = asList(
                JsrId.of(370),
                JsrId.of(339),
                JsrId.of(311));

        assertThat(
                toIds(idx.queryAllByPackage("javax.ws.rs")),
                equalTo(jaxRsJsrIds));
    }
    
    @Test
    public void jaxRsClientJsrs() {
        // JSR 339 introduced javax.ws.rs.client in JAX-RS 2.0, it is not specified by JSR 311.
        Collection<JsrId> jaxRsClientJsrs = asList(JsrId.of(370), JsrId.of(339));
        assertThat(
                toIds(idx.queryAllByPackage("javax.ws.rs.client")),
                equalTo(jaxRsClientJsrs));
    }
}
