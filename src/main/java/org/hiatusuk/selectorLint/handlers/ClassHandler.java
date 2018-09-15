package org.hiatusuk.selectorLint.handlers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hiatusuk.selectorLint.ElementContext;
import org.hiatusuk.selectorLint.config.Rules;
import org.hiatusuk.selectorLint.config.filters.RulesBasedFilter;
import org.hiatusuk.selectorLint.impl.MatchTester;
import org.hiatusuk.selectorLint.impl.NodeAdder;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
import org.hiatusuk.selectorLint.tree.Path;
import org.hiatusuk.selectorLint.utils.CssUtils;
import org.openqa.selenium.By;

public class ClassHandler extends AbstractBaseHandler {

    private final Predicate<String> ignoreClassNames;
    private final int minAcceptableClassLength;

    public ClassHandler(final Rules rules, final List<String> ignoreClassNames, final int minAcceptableClassLength) {
        this.ignoreClassNames = new RulesBasedFilter(rules, ignoreClassNames);
        this.minAcceptableClassLength = minAcceptableClassLength;
    }

    @Override
    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {
        if (ctxt.currentTagName().equals("body") || ctxt.currentTagName().equals("main") || ctxt.currentTagName().equals("html") || ctxt.currentTagName().equals("head")) {
            return false;
        }

        boolean gotGoodClass = false;

        // Need to filter, or at least *score* these!
        for (String eachClass : filter( ctxt.attributes().get("class") )) {  // Will never be empty strings

            ctxt.setHasSomeProps();

            if (ctxt.isLeaf()) {
                // Best of all: more testable than By.className, handles multiple classnames
                if (tester.ok( By.cssSelector( ctxt.currentTagName() + "." + CssUtils.cssEscape(eachClass)) )) { 
                    continue;
                }
            }

            final Node newNode = nodes.add( ctxt.currentTagName() + "." + CssUtils.cssEscape(eachClass), true);

            for (Path each : new NodeVisitor().visit(newNode)) {
                if (tester.ok( By.cssSelector( each.getPath() ) )) {
                    return true; // FIXME Should set flag and *not* exit, i.e. gotGoodClass = true;
                }
            }
        }

        return gotGoodClass;
    }

    private Iterable<String> filter(final String classStr) {
        if (classStr == null || classStr.isEmpty()) {
            return Collections.emptyList();
        }

        // FIXME Either filter by quality or *score*
        return Arrays.stream( classStr.split(" ") )
                .filter(inClass -> ignoreClassNames.test(inClass) && inClass.length() >= minAcceptableClassLength)
                .collect(Collectors.toList());
    }
}