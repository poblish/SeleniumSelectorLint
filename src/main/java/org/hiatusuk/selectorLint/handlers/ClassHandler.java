package org.hiatusuk.selectorLint.handlers;

import java.util.Set;

import org.hiatusuk.selectorLint.*;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
import org.hiatusuk.selectorLint.tree.Path;
import org.openqa.selenium.By;

public class ClassHandler implements ElementHandler {

    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {
        // Need to filter, or at least *score* these!
        for (String eachClass : Classes.filter( ctxt.attributes().get("class") )) {  // Will never be empty strings

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
                    return true;
                }
            }
        }

        return false;
    }
}