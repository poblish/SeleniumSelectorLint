package org.hiatusuk.selectorLint;

import java.util.regex.Pattern;

public class Semantic {

    public final static Pattern PATT = Pattern.compile("^(id-|:|\\\\3A )");

    public static boolean isNonSemantic( String value) {
        return value.equals("0") || value.equals("1") || isGeneratedString(value);
    }

    public static boolean isGeneratedString(final String val) {
        return /* Hack: */ val.equals("afn") || PATT.matcher(val).find();
    }
}
