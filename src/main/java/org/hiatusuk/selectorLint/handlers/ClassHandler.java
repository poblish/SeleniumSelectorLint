package org.hiatusuk.selectorLint.handlers;

import static com.google.common.base.Predicates.in;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hiatusuk.selectorLint.*;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
import org.hiatusuk.selectorLint.tree.Path;
import org.hiatusuk.selectorLint.utils.CssUtils;
import org.openqa.selenium.By;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ClassHandler extends AbstractBaseHandler {

    private final Predicate<String> ignoreClassNames;
    private final int minAcceptableClassLength;

    public ClassHandler(final List<String> ignoreClassNames, final int minAcceptableClassLength) {
        this.ignoreClassNames = in(ignoreClassNames);
        this.minAcceptableClassLength = minAcceptableClassLength;
    }

    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {
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

            final Set<Path> paths = new NodeVisitor().visit(newNode);
            // System.out.println("::: CLASS: " + paths.size() + " paths: " + paths);

            for (Path each : paths) {
                if (tester.ok( By.cssSelector( each.getPath() ) )) {
                    return true; // FIXME Should set flag and *not* exit, i.e. gotGoodClass = true;
                }
            }
        }

        return gotGoodClass;
    }

    private final Predicate<String> acceptRule = new Predicate<String>() {

        @Override
        public boolean apply(final String inClass) {
            if (ignoreClassNames.apply(inClass) || inClass.length() < minAcceptableClassLength || Semantic.isNonSemantic(inClass)) {
                return false;
            }

            return true;
        }};

    private Iterable<String> filter(final String classStr) {
        if (classStr == null || classStr.isEmpty()) {
            return Collections.emptyList();
        }

        // FIXME Either filter by quality or *score*
        return Iterables.filter( Arrays.asList( classStr.split(" ") ), acceptRule);
    }
}