package org.hiatusuk.selectorLint;

public class Classes {

    private static final String[] EMPTY = new String[] {};

    public static String[] filter(final String classStr) {
        if (classStr == null || classStr.isEmpty()) {
            return EMPTY;
        }

        // FIXME Either filter by quality or *score*
        return classStr.split(" ");
    }
}
