package org.hiatusuk.selectorLint.handlers;

import java.util.Arrays;
import java.util.Set;

import org.hiatusuk.selectorLint.ElementContext;
import org.hiatusuk.selectorLint.MatchTester;
import org.hiatusuk.selectorLint.NodeAdder;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
import org.hiatusuk.selectorLint.tree.Path;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class TagHandler implements ElementHandler {

    private final Predicate<String> semanticTags;

    public TagHandler(final String... semanticTags) {
        this.semanticTags = Predicates.in(Arrays.asList(semanticTags));
    }

    private boolean isGoodQuality( WebElement elem) {
        return semanticTags.apply( elem.getTagName().toLowerCase() );
    }

    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {
        if (!isGoodQuality( ctxt.element() )) {
            return false;
        }

        if (ctxt.isLeaf()) {
            if (ctxt.getOriginalSelector().equalsIgnoreCase("By.cssSelector: " + ctxt.currentTagName()) ||
                ctxt.getOriginalSelector().equalsIgnoreCase("By.tagName: " + ctxt.currentTagName())) {
                // If CSS 'body' was passed in, don't propose pointlessly changing to tagName syntax
                return true;
            }
        
            if (tester.ok( By.tagName( ctxt.currentTagName() ) )) {
                /* FIXME */ return true;
            }
        }

        final Node newNode = nodes.add( ctxt.currentTagName(), true);

        ctxt.setHasSomeProps();
        ctxt.setAddedGoodNonUniqueNode();

        final Set<Path> paths = new NodeVisitor().visit(newNode);
        // System.out.println("::: TAG: " + paths.size() + " paths: " + paths);

        for (Path each : paths) {
            if (tester.ok( By.cssSelector( each.getPath() ) )) {
                return true;
            }
        }

        return false;
    }
}