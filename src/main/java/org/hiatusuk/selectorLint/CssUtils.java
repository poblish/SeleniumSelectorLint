package org.hiatusuk.selectorLint;

public class CssUtils {

    // From org.openqa.selenium.remote.RemoteWebDriver
    public static String cssEscape( String using) {
        using = using.replaceAll("(['\"\\\\#.:;,!?+<>=~*^$|%&@`{}\\-\\/\\[\\]\\(\\)])", "\\\\$1");
        if (using.length() > 0 && Character.isDigit(using.charAt(0))) {
            using = "\\" + Integer.toString(30 + Integer.parseInt(using.substring(0, 1))) + " " + using.substring(1);
        }
        return using;
    }
}
