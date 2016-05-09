package org.hiatusuk.selectorLint.handlers;

import java.util.Set;

import org.hiatusuk.selectorLint.*;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
import org.hiatusuk.selectorLint.tree.Path;
import org.hiatusuk.selectorLint.utils.Strings;
import org.openqa.selenium.By;

public class IdsHandler implements ElementHandler {

    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {

        final String id = ctxt.attributes().get("id");
        // Need a quality/generated check on this Id!

        if (Strings.hasString(id) && !Semantic.isGeneratedString(id)) {
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

            final Set<Path> paths = new NodeVisitor().visit(newNode);
            // System.out.println("::: " + paths.size() + " paths: " + paths);

            for (Path each : paths) {
                final By sel = By.cssSelector( each.getPath() );
                if (ctxt.getOriginalSelector().equals( sel.toString() )) {
                    return true;
                }

                tester.ok(sel);  // assume/require PASS
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldSkip( String tagName) {  // Ugly, FIXME
        return false;
    }
}