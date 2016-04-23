package org.hiatusuk.selectorLint;

import static com.google.common.base.Predicates.in;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class Attributes {

    private static Predicate<String> IGNORE_KEYS = in(Arrays.asList("class","id","disabled","style","gh","cellpadding","tabindex","lang","onclick"));
    private static Predicate<String> KEYS_NEED_SEMANTIC_VALUE = in(Arrays.asList("aria-labelledby"));

    private static Predicate<Entry<String,String>> ACCEPT = new Predicate<Entry<String,String>>() {

        @Override
        public boolean apply(final Entry<String,String> inEntry) {
            if (IGNORE_KEYS.apply(inEntry.getKey())) {
                return false;
            }

            if (KEYS_NEED_SEMANTIC_VALUE.apply(inEntry.getKey()) && isNonSemantic( inEntry.getValue() )) {
                return false;
            }

            return true;
        }};

    public static boolean isNonSemantic( String value) {
        return (value.equals("0") || value.equals("1") || Ids.PATT.matcher(value).find());
    }

    public static Map<String,String> filterQuality(final Map<String,String> attrs) {
        return Maps.filterEntries(attrs, ACCEPT);
    }
}
