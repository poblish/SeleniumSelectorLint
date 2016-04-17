package org.hiatusuk.selectorLint;

import java.util.regex.Pattern;

// FIXME Needs to be a configurable rule
public class Ids {

    private final static Pattern PATT = Pattern.compile("^(id-|:)");

    public static boolean isGeneratedString(final String val) {
        return PATT.matcher(val).find();
    }
}
