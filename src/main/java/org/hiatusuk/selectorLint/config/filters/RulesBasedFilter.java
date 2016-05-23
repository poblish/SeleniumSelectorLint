package org.hiatusuk.selectorLint.config.filters;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Pattern;

import org.hiatusuk.selectorLint.config.Rules;

import com.google.common.base.Predicate;

public class RulesBasedFilter implements Predicate<String>, Serializable {
    private final Rules rules;
    private final Collection<String> target;

    public RulesBasedFilter(final Rules inRules, final Collection<String> target) {
        this.rules = checkNotNull(inRules);
        this.target = checkNotNull(target);
    }

    public boolean isEmpty() {
        return target.isEmpty();
    }

    @Override
    public boolean apply(final String arg) {
//        if (target.isEmpty()) {
//            return true;
//        }

        for (String eachArg : target) {
            if (eachArg.startsWith("^")) {
                final String actualName = eachArg.substring(1);

                if (!rules.containsKey(actualName)) {
                    continue;
                }

                if (!rules.get(actualName).apply(arg)) {
//                  System.out.println("* FALSE for rule: " + eachArg + " vs " + arg);
                    return false;
                }
            }
            else if (eachArg.equals(arg)) {
//              System.out.println("* FALSE for string: " + eachArg + " vs " + arg);
                return false;
            }
            else if (eachArg.startsWith("/")) {  // FIXME Should really pre-process these Pattern-strings to save recompiling
                if (Pattern.compile(eachArg.substring(1, eachArg.length() - 1)).matcher(arg).find()) {
                    return false;
                }
            }
        }
//            System.out.println("* TRUE for " + arg);
        return true;
    }

    private static final long serialVersionUID = 0;
}
