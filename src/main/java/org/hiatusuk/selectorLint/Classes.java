package org.hiatusuk.selectorLint;

import static com.google.common.base.Predicates.in;

import java.util.Arrays;
import java.util.Collections;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class Classes {

    private static Predicate<String> IGNORE_CLASSES = in(Arrays.asList("","clear-fix","bold","blue"));

    private static Predicate<String> ACCEPT = new Predicate<String>() {

        @Override
        public boolean apply(final String inClass) {
            if (IGNORE_CLASSES.apply(inClass) || inClass.length() < 3 || Semantic.isNonSemantic(inClass)) {
                return false;
            }

            return true;
        }};

    public static Iterable<String> filter(final String classStr) {
        if (classStr == null || classStr.isEmpty()) {
            return Collections.emptyList();
        }

        // FIXME Either filter by quality or *score*
        return Iterables.filter( Arrays.asList( classStr.split(" ") ), ACCEPT);
    }
}
