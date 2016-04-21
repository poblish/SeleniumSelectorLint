package org.hiatusuk.selectorLint;

import java.util.Arrays;

import org.openqa.selenium.WebElement;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class Tags {

    private static final Predicate<String> SEMANTIC_TAGS = Predicates.in(Arrays.asList("article","body","footer","head","table"));

    public static boolean isGoodQuality( WebElement elem) {
        return SEMANTIC_TAGS.apply( elem.getTagName().toLowerCase() );
    }
}
