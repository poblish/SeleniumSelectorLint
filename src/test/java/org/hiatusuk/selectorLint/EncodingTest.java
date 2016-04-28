package org.hiatusuk.selectorLint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hiatusuk.selectorLint.CssUtils.cssEscape;

import org.testng.annotations.Test;

public class EncodingTest {

    @Test public void testEncoding() {
        assertThat( cssEscape(""), is(""));
        assertThat( cssEscape("#"), is("\\#"));
        assertThat( cssEscape("243#"), is("\\32 43#"));
        assertThat( cssEscape("Hello"), is("Hello"));
        assertThat( cssEscape("Foo:Bar"), is("Foo\\:Bar"));
        assertThat( cssEscape("Foo^Bar"), is("Foo\\^Bar"));
        assertThat( cssEscape("Foo=Bar"), is("Foo\\=Bar"));
        assertThat( cssEscape("Foo$Bar"), is("Foo\\$Bar"));
    }
}
