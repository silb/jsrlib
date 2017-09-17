package org.secnod.jsr.index;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;
import org.secnod.jsr.Jsr;
import org.secnod.jsr.JsrId;

public class InMemoryJsrIndexTest {

    @Test
    public void inMemoryIndex() throws Exception {
        Jsr e = new Jsr();
        e.id = JsrId.of(215);
        e.packages = new HashSet<>(asList("javax.servlet"));

        Jsr e2 = new Jsr();
        e2.id = JsrId.of(315);
        e2.succeeds = JsrId.of(215);
        e2.packages = new HashSet<>(asList("javax.servlet"));

        Jsr e3 = new Jsr();
        e3.id = JsrId.of(415);
        e3.succeeds = JsrId.of(315);

        Jsr e4 = new Jsr();
        e4.id = JsrId.of(515);
        e4.packages = new HashSet<>(asList("javax.servlet"));

        JsrIndex idx = new JsrIndex.Builder().data(new HashSet<>(asList(e, e2, e4, e3))).build();

        assertThat(idx.queryAllByPackage("javax.servlet"), equalTo(asCollection(e4, e3, e2, e)));
        assertThat(idx.queryAllByPackage("javax.servlet.http"), equalTo(asCollection(e4, e3, e2, e)));
    }

    @SafeVarargs
    static <T> Collection<T> asCollection(T... elements) {
        return Arrays.asList(elements);
    }
}
