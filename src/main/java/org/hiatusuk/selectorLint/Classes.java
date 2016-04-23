package org.hiatusuk.selectorLint;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;

import java.util.Arrays;
import java.util.Collections;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class Classes {

    private static Predicate<String> ACCEPT = not(in(Arrays.asList("","clear-fix","bold","blue")));

    public static Iterable<String> filter(final String classStr) {
        if (classStr == null || classStr.isEmpty()) {
            return Collections.emptyList();
        }

        // FIXME Either filter by quality or *score*
        return Iterables.filter( Arrays.asList( classStr.split(" ") ), ACCEPT);
    }
}
