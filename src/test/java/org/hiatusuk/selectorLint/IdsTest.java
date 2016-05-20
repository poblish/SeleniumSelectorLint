package org.hiatusuk.selectorLint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.regex.Pattern;

import org.testng.annotations.Test;

public class IdsTest {

    @Test
    public void testGeneratedIds() {
        testValidIds("okId", "form:element", "ctl00_cph1_Login1_txtPassword");
        testInvalidIds(":4d", "id-23467233", "id-ad4ad45e");
    }

    private void testValidIds(final String...ids) {
        for (String each : ids) {
            assertThat( isGeneratedString(each), is(false));
        }
    }

    private void testInvalidIds(final String...ids) {
        for (String each : ids) {
            assertThat( isGeneratedString(each), is(true));
        }
    }

    private final static Pattern PATT = Pattern.compile("^(id-|:|\\\\3A )");

    public static boolean isGeneratedString(final String val) {
        return /* Hack: */ val.equals("afn") || PATT.matcher(val).find();
    }
}
