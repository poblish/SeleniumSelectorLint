package org.hiatusuk.selectorLint.handlers;

import java.util.List;
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

public class TagHandler extends AbstractBaseHandler {

    private final Predicate<String> semanticTags;
    private final Predicate<String> ignoreTags;

    public TagHandler(final List<String> semanticTags, final List<String> ignoreTags) {
        this.semanticTags = Predicates.in(semanticTags);
        this.ignoreTags = Predicates.in(ignoreTags);
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

        final Node newNode = nodes.add( ctxt.currentTagName(), !ctxt.skippedUselessElement());

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

    @Override
    public boolean shouldSkip(final String tagName) {
        return ignoreTags.apply(tagName);
    }
}