package org.hiatusuk.selectorLint.handlers;

import java.util.List;
import java.util.function.Predicate;

import org.hiatusuk.selectorLint.*;
import org.hiatusuk.selectorLint.config.Options;
import org.hiatusuk.selectorLint.config.filters.RulesBasedFilter;
import org.hiatusuk.selectorLint.impl.MatchTester;
import org.hiatusuk.selectorLint.impl.NodeAdder;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
import org.hiatusuk.selectorLint.tree.Path;
import org.hiatusuk.selectorLint.utils.Strings;
import org.openqa.selenium.By;

public class IdsHandler extends AbstractBaseHandler {

    private final Predicate<String> ignoreItems;

    public IdsHandler(final Options opts, final List<String> ignoreItems) {
        this.ignoreItems = new RulesBasedFilter(opts.getNamedFilters(), ignoreItems);
    }

    @Override
    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {

        final String id = ctxt.attributes().get("id");
        // Need a quality/generated check on this Id!

        if (Strings.hasString(id) && ignoreItems.test(id)) {
            if (ctxt.getOriginalSelector().equals("By.id: " + id) ||
                ctxt.getOriginalSelector().equals("By.cssSelector: #" + id) ||
                ctxt.getOriginalSelector().equals("By.xpath: //*[@id=\"" + id + "\"]") ||
                ctxt.getOriginalSelector().equals("By.cssSelector: " + ctxt.currentTagName() + "#" + id) ||
                ctxt.getOriginalSelector().equals("By.xpath: //" + ctxt.currentTagName() + "[@id=\"" + id + "\"]")) {
                // Potential Id query identical to existing CSS query, so skip
                return true;
            }

            if (ctxt.isLeaf()) {
                tester.ok( By.id(id) );  // assume/require PASS
                return true;
            }

            final Node newNode = nodes.add("#" + id, true);

            for (Path each : new NodeVisitor().visit(newNode)) {

                final By sel = By.cssSelector( each.getPath() );
                if (ctxt.getOriginalSelector().equals( sel.toString() )) {
                    return true;
                }

                if (tester.ok(sel)) {
                    return true;
                }
            }
        }

        return false;
    }
}