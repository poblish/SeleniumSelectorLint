package org.hiatusuk.selectorLint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.hiatusuk.selectorLint.utils.Uuids;

import com.google.common.base.Predicate;

public class FilterPredicate implements Predicate<String> {

    final private List<String> ignored = new ArrayList<>();

    public FilterPredicate(final Map<String,List<String>> elems) {
        // FIXME Only 'ignore' supported for time being
        final List<String> ignored = elems.get("ignore");
        if (ignored != null) {
            this.ignored.addAll(ignored);
        }
    }

    @Override
    public boolean apply(final String input) {
        for (String eachIgnoreItem : ignored) {
            if (eachIgnoreItem.startsWith("/")) {  // FIXME Should really pre-process these Pattern-strings to save recompiling
                // System.out.println("Found pattern: " + eachIgnoreItem);
                if (Pattern.compile(eachIgnoreItem.substring(1, eachIgnoreItem.length() - 1)).matcher(input).find()) {
                    return false;
                }
            }
            else if (eachIgnoreItem.equals("^emptyValue")) {
                if (input.isEmpty()) {
                    return false;
                }
            }
            else if (eachIgnoreItem.equals("^uuids")) {
                if (Uuids.isUUID(input)) {
                    return false;
                }
            }
            else {
                if (eachIgnoreItem.equals(input)) {
                    return false;
                }
            }
        }
        return true;
    }
}
