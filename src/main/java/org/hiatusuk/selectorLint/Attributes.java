package org.hiatusuk.selectorLint;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class Attributes {

    private static Predicate<String> ACCEPT = not(in(Arrays.asList("class","id","disabled","style","gh","cellpadding","tabindex","lang","onclick")));

    public Attributes() {
    }

    public static Map<String,String> filterQuality(final Map<String,String> attrs) {
        return Maps.filterKeys(attrs, ACCEPT);
    }
}
