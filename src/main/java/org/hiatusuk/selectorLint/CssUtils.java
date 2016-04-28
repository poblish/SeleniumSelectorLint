package org.hiatusuk.selectorLint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssUtils {

    private final static Pattern NEEDS_ENCODING = Pattern.compile("(['\"\\\\#.:;,!?+<>=~*^$|%&@`{}\\-\\/\\[\\]\\(\\)])");

    // Derived org.openqa.selenium.remote.RemoteWebDriver
    public static String cssEscape(final String using) {
        final Matcher m = NEEDS_ENCODING.matcher(using);
        final String result = m.replaceAll("\\\\$1");
        if (result.length() > 0 && Character.isDigit(using.charAt(0))) {
            return "\\" + Integer.toString(30 + Integer.parseInt(using.substring(0, 1))) + " " + using.substring(1);
        }
        return result;
    }
}
