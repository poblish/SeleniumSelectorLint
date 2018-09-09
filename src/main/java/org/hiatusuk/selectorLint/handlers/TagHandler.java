package org.hiatusuk.selectorLint.handlers;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.hiatusuk.selectorLint.ElementContext;
import org.hiatusuk.selectorLint.config.Rules;
import org.hiatusuk.selectorLint.config.filters.RulesBasedFilter;
import org.hiatusuk.selectorLint.impl.MatchTester;
import org.hiatusuk.selectorLint.impl.NodeAdder;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
import org.hiatusuk.selectorLint.tree.Path;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class TagHandler extends AbstractBaseHandler {

    private final Predicate<String> semanticTags;
    private final Predicate<String> ignoreTags;

    public TagHandler(final Rules rules, final List<String> semanticTags, final List<String> ignoreTags) {
        this.semanticTags = new RulesBasedFilter(rules, semanticTags);
        this.ignoreTags = new RulesBasedFilter(rules, ignoreTags);
    }

    private boolean isMediocreQuality( WebElement elem) {
        return semanticTags.test( elem.getTagName().toLowerCase() );
    }

    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {
//        System.out.println("-- TAG Mediocre? " + isMediocreQuality( ctxt.element() ) + " for " + ctxt.element());
        if (isMediocreQuality( ctxt.element() )) {
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
        return !ignoreTags.test(tagName);
    }
}