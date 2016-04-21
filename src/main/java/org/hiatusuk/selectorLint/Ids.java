package org.hiatusuk.selectorLint;

import java.util.regex.Pattern;

// FIXME Needs to be a configurable rule
public class Ids {

    public final static Pattern PATT = Pattern.compile("^(id-|:|\\\\3A )");

    public static boolean isGeneratedString(final String val) {
        return PATT.matcher(val).find();
    }
}
