package org.secnod.jsr.index;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.secnod.jsr.Jsr;
import org.secnod.jsr.JsrId;

public class InMemoryJsrIndexTest {

    @Test
    public void inMemoryIndex() throws Exception {
        Jsr jsr200 = new Jsr(200);
        jsr200.packages = Set.of("javax.servlet");

        Jsr jsr201 = new Jsr(201);
        jsr201.succeeds = jsr200.id;
        jsr201.packages = Set.of("javax.servlet");

        Jsr jsr202 = new Jsr(202);
        jsr202.succeeds = jsr201.id;

        Jsr jsr300 = new Jsr(300);
        jsr300.packages = Set.of("javax.servlet");

        JsrIndex idx = new JsrIndex.Builder().data(Set.of(jsr200, jsr201, jsr300, jsr202)).build();

        assert Collections.lastIndexOfSubList(new ArrayList<>(idx.queryAllByPackage("javax.servlet")), List.of(jsr202, jsr201, jsr200)) != -1 : "Incorrect partial ordering of related JSRs";
        assertThat(Set.copyOf(idx.queryAllByPackage("javax.servlet")), equalTo(Set.of(jsr300, jsr202, jsr201, jsr200)));

        assert Collections.lastIndexOfSubList(new ArrayList<>(idx.queryAllByPackage("javax.servlet.http")), List.of(jsr202, jsr201, jsr200)) != -1 : "Incorrect partial ordering of related JSRs";
        assertThat(Set.copyOf(idx.queryAllByPackage("javax.servlet.http")), equalTo(Set.of(jsr300, jsr202, jsr201, jsr200)));
    }

    @Test
    public void anyIndexOrder() throws Exception {
        Jsr jsr200 = new Jsr(200);
        jsr200.packages = Set.of("javax.servlet");
        Jsr jsr201 = new Jsr(201);
        jsr201.succeeds = jsr200.id;
        Jsr jsr202 = new Jsr(202);
        jsr202.succeeds = jsr201.id;

        new JsrIndex.Builder().data(List.of(jsr200, jsr201, jsr202)).build();
        new JsrIndex.Builder().data(List.of(jsr200, jsr202, jsr201)).build();
        new JsrIndex.Builder().data(List.of(jsr201, jsr200, jsr202)).build();
        new JsrIndex.Builder().data(List.of(jsr201, jsr202, jsr200)).build();
        new JsrIndex.Builder().data(List.of(jsr202, jsr200, jsr201)).build();
        new JsrIndex.Builder().data(List.of(jsr202, jsr201, jsr200)).build();
    }

    @Test
    public void lineageOrder() {
        Jsr jsr914 = new Jsr(914);
        jsr914.packages = Set.of("javax.jms");

        Jsr jsr343 = new Jsr(343);
        jsr343.succeeds = jsr914.id;

        Jsr jsr400 = new Jsr(400);
        jsr400.succeeds = jsr343.id;

        JsrIndex idx = new JsrIndex.Builder().data((Set.of(jsr914, jsr400, jsr343))).build();

        assertThat(idx.queryAllByPackage("javax.jms"), equalTo(List.of(jsr400, jsr343, jsr914)));
    }

    @Test
    public void umbrellaTags() {
        var jsr1 = new Jsr(100);
        jsr1.packages = Set.of("javax.jsr1");
        var jsr2 = new Jsr(200);
        jsr2.packages = Set.of("javax.jsr2");

        var umbrella = new Jsr(300);
        umbrella.tags = Set.of("tag1");
        umbrella.umbrella = Set.of(jsr1.id, jsr2.id);

        JsrIndex idx = new JsrIndex.Builder().data((Set.of(umbrella, jsr1, jsr2))).build();
        assertThat(idx.queryByTag("tag1"), equalTo(Set.of(jsr1, jsr2, umbrella)));
    }

    @Test(expected = IllegalStateException.class)
    public void missingSuccessor() {
        var jsr1 = new Jsr(100);
        jsr1.packages = Set.of("javax.jsr1");
        var jsr2 = new Jsr(200);
        jsr2.succeeds = JsrId.of(101);
        new JsrIndex.Builder().data((Set.of(jsr1, jsr2))).build();
    }

    @Test(expected = IllegalStateException.class)
    public void missingUmbrelled() {
        var jsr1 = new Jsr(100);
        jsr1.packages = Set.of("javax.jsr1");
        var jsr2 = new Jsr(200);
        jsr2.umbrella = Set.of(JsrId.of(100), JsrId.of(101));
        jsr2.tags = Set.of("tag1", "tag2");
        new JsrIndex.Builder().data((Set.of(jsr1, jsr2))).build();
    }
}
