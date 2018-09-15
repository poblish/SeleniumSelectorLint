package org.hiatusuk.selectorLint.handlers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hiatusuk.selectorLint.ElementContext;
import org.hiatusuk.selectorLint.config.Rules;
import org.hiatusuk.selectorLint.config.filters.FilterPredicate;
import org.hiatusuk.selectorLint.impl.MatchTester;
import org.hiatusuk.selectorLint.impl.NodeAdder;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
import org.hiatusuk.selectorLint.tree.Path;
import org.openqa.selenium.By;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class AttributesHandler extends AbstractBaseHandler {

    private final Predicate<String> ignoreKeys;
    private final Map<String,List<String>> keysNeedSemanticValue;
    private final Rules rules;

    public AttributesHandler(final Rules rules, final List<String> ignoreAttributes, final Map<String,List<String>> keysNeedSemanticValue) {
        this.ignoreKeys = Predicates.in(ignoreAttributes); // new RulesBasedFilter(rules, ignoreAttributes);
        this.keysNeedSemanticValue = keysNeedSemanticValue;
        this.rules = rules;
    }

    @Override
    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {
        if (ctxt.currentTagName().equals("body") || ctxt.currentTagName().equals("main") || ctxt.currentTagName().equals("html") || ctxt.currentTagName().equals("head")) {
            return false;
        }

        boolean gotResult = false;

        for (Entry<String, String> eachAttr : ctxt.attributes().entrySet()) {
            if (ignoreKeys.apply(eachAttr.getKey())) {
                continue;
            }

            // System.out.println(">>> TRY: " + ctxt.currentTagName() + "." + eachAttr.getKey());

            boolean filtered = false;
            for (Entry<String,List<String>> eachSV : keysNeedSemanticValue.entrySet()) {

                FilterPredicate fp = rules.get( eachSV.getKey().substring(1) );

                // Is our attribute registered as one we should check? *Then* we can validate its value
                if (eachSV.getValue().contains( ctxt.currentTagName() + "." + eachAttr.getKey() ) ||
                    eachSV.getValue().contains( eachAttr.getKey() )) {
                    // System.out.println(">>> FOUND for " + eachSV.getKey());

                    if (!fp.apply( eachAttr.getValue() )) {
                        // System.out.println(">>> SKIPPING " + ctxt.currentTagName() + "." + eachAttr.getKey() + ", contained in " + eachSV.getValue());
                        filtered = true;
                        break;
                    }
                }
            }

            if (filtered) {
                continue;
            }

            ctxt.setHasSomeProps();

            if (ctxt.isLeaf()) {
                // By trying = By.xpath(".//*[@" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                if (tester.ok( By.cssSelector( ctxt.currentTagName() + "[" + eachAttr.getKey() + "='" + eachAttr.getValue() + "']") )) {
                    continue;
                }
            }

            final Node newNode = nodes.add( ctxt.currentTagName() + "[" + eachAttr.getKey() + "='" + eachAttr.getValue() + "']", true);

            for (Path each : new NodeVisitor().visit(newNode)) {
                if (tester.ok( By.cssSelector( each.getPath() ) )) {
                    gotResult = true;
                }
            }
        }

        return gotResult;
    }
}