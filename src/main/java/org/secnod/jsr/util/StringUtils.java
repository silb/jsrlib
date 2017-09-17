package org.secnod.jsr.util;

import java.util.Collection;
import java.util.Iterator;

public final class StringUtils {

    private StringUtils() {}

    public static String toString(Collection<?> c, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<?> i = c.iterator(); i.hasNext();) {
            sb.append(i.next());
            if (i.hasNext())
                sb.append(separator);
        }
        return sb.toString();
    }

}
