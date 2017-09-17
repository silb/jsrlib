package org.secnod.jsr;

/**
 * @see <a href="https://www.jcp.org/en/introduction/glossary#status">https://www.jcp.org/en/introduction/glossary#status</a>
 */
public enum JsrStatus {
    ACTIVE,
    FINAL,
    MAINTENANCE,
    INACTIVE,
    WITHDRAWN,
    REJECTED,
    DORMANT;

    public static JsrStatus parse(String s) {
        if (s == null) return null;
        try {
            return JsrStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {}
        return null;
    }

    public String label() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
