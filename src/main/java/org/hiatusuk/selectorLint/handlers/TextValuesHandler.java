package org.hiatusuk.selectorLint.handlers;

import java.util.List;

import org.hiatusuk.selectorLint.ElementContext;
import org.hiatusuk.selectorLint.config.Rules;
import org.hiatusuk.selectorLint.config.filters.RulesBasedFilter;
import org.hiatusuk.selectorLint.impl.MatchTester;
import org.hiatusuk.selectorLint.impl.NodeAdder;
import org.hiatusuk.selectorLint.utils.Strings;
import org.openqa.selenium.By;

public class TextValuesHandler extends AbstractBaseHandler {

    private final RulesBasedFilter onlyTags;
    private final RulesBasedFilter ignoreTags;

    public TextValuesHandler(final Rules rules, final List<String> onlyTags, final List<String> ignoreTags) {
        this.onlyTags = new RulesBasedFilter(rules, onlyTags);
        this.ignoreTags = new RulesBasedFilter(rules, ignoreTags);
    }

    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {

        final String elemStr = ctxt.element().getText();

        if (!Strings.hasString(elemStr)) { // Try text
            return false;
        }

        // Exit if we've set an "Only these tags" list, and the current Tag *wasn't* found
        if (!onlyTags.isEmpty() && onlyTags.apply( ctxt.currentTagName() )) {
            // System.out.println("TV Requires ONLY text for " + ctxt.currentTagName());
            return false;
        }

        // Exit if we've set an "Ignore these tags" list, and the current Tag *was* found
        if (!ignoreTags.isEmpty() && !ignoreTags.apply( ctxt.currentTagName() )) {
            // System.out.println("TV Should IGNORE text for " + ctxt.currentTagName());
            return false;
        }

        // System.out.println("*** Want 'text' for " + ctxt.currentTagName());

        ctxt.setHasSomeProps();

        if (ctxt.isLeaf()) {
//            if (ctxt.getOriginalSelector().equalsIgnoreCase("By.cssSelector: " + ctxt.currentTagName()) ||
//                ctxt.getOriginalSelector().equalsIgnoreCase("By.tagName: " + ctxt.currentTagName())) {
//                // If CSS 'body' was passed in, don't propose pointlessly changing to tagName syntax
//                return true;
//            }
        
            if (tester.ok( By.xpath("//" + ctxt.currentTagName() + "[text()='" + elemStr + "']") )) {
                return true;
            }
        }

//        final Node newNode = nodes.add( "//" + ctxt.currentTagName() + "[text()='" + elemStr + "']", true);
//
//        ctxt.setHasSomeProps();
//        ctxt.setAddedGoodNonUniqueNode();
//
//        final Set<Path> paths = new NodeVisitor().visit(newNode);
//        // System.out.println("::: TAG: " + paths.size() + " paths: " + paths);
//
//        for (Path each : paths) {
//            if (tester.ok( By.xpath( each.getPath() ) )) {
//                return true;
//            }
//        }

        return false;
    }
}