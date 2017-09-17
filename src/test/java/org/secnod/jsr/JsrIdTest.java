package org.secnod.jsr;

import static org.junit.Assert.*;

import org.junit.Test;

public class JsrIdTest {

    @Test
    public void variants() {
        assertEquals(JsrId.of(53, "servlet"), JsrId.of(53, "Servlet"));
    }

    @Test
    public void parsing() {
        assertEquals(JsrId.of(168), JsrId.of("168"));
        assertEquals(JsrId.of(53, "servlet"), JsrId.of("53 servlet"));
        assertEquals(JsrId.of(53, "servlet"), JsrId.of("53, servlet"));
        assertEquals(JsrId.of(53, "servlet"), JsrId.of("53 , servlet "));
        assertEquals(JsrId.of(53, "servlet"), JsrId.of("53-servlet "));

        assertNull(JsrId.of(""));
        assertNull(JsrId.of((String) null));
        assertNull(JsrId.of("abc"));
    }
}
