package org.secnod.jsr;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsrId implements Comparable<JsrId> {

    public final Integer jsrNumber;
    public final String variant;

    private static final Pattern jsrSpecPattern = Pattern.compile("\\s*(\\d+)(\\s*[\\s,-]\\s*(\\w+))?\\s*");

    private JsrId(Integer jsrNumber, String variant) {
        this.jsrNumber = jsrNumber;
        this.variant = variant != null ? variant.toLowerCase() : "";
    }

    public boolean hasVariant() {
        return !variant.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsrNumber, variant);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof JsrId))
            return false;
        return compareTo((JsrId) obj) == 0;
    }

    @Override
    public int compareTo(JsrId o) {
        int jsrComp = Integer.compare(jsrNumber, o.jsrNumber);
        if (jsrComp != 0)
            return jsrComp;
        else if (variant == null)
            return -1;
        else if (variant != null && o.variant == null)
            return 1;
        else if (variant != null && o.variant != null)
            return variant.compareTo(o.variant);
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(jsrNumber);
        if (!variant.isEmpty())
            sb.append('-').append(variant);
        return sb.toString();
    }

    public String displayName() {
        StringBuilder sb = new StringBuilder("JSR ");
        sb.append(jsrNumber);
        if (!variant.isEmpty())
            sb.append(" (").append(variant).append(")");
        return sb.toString();
    }

    public static JsrId of(Integer jsrNumber, String variant) {
        return jsrNumber != null ? new JsrId(jsrNumber, variant) : null;
    }

    public static JsrId of(Integer jsrNumber) {
        return jsrNumber != null ? new JsrId(jsrNumber, null) : null;
    }

    public static JsrId of(String spec) {
        if (spec == null) return null;
        Matcher m = jsrSpecPattern.matcher(spec);
        if (!m.matches()) return null;
        Integer jsrNumber = Integer.valueOf(m.group(1));
        String jsrVariant = m.group(3);
        return new JsrId(jsrNumber, jsrVariant);
    }
}
