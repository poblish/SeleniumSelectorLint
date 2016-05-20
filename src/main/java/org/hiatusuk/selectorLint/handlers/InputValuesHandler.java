package org.hiatusuk.selectorLint.handlers;

import java.util.List;
import java.util.Map.Entry;

import org.hiatusuk.selectorLint.ElementContext;
import org.hiatusuk.selectorLint.MatchTester;
import org.hiatusuk.selectorLint.NodeAdder;
import org.hiatusuk.selectorLint.Options.Rules;
import org.hiatusuk.selectorLint.RulesBasedFilter;
import org.openqa.selenium.By;

import com.google.common.base.Predicate;

public class InputValuesHandler extends AbstractBaseHandler {

    private final Predicate<String> ignoreRuleNames;

    public InputValuesHandler(final Rules rules, final List<String> ignoreRuleNames) {
        this.ignoreRuleNames = new RulesBasedFilter(rules, ignoreRuleNames);
    }

    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {

        for (Entry<String, String> eachGoodAttr : ctxt.attributes().entrySet()) {
            if (!ctxt.currentTagName().equals("input") || !eachGoodAttr.getKey().equals("value")) {
                continue;  // ignore
            }

            if (!this.ignoreRuleNames.apply( eachGoodAttr.getValue() )) {
                continue;  // non-semantic
            }

            ctxt.setHasSomeProps();

            if (ctxt.isLeaf()) {
                if (tester.ok( By.cssSelector( ctxt.currentTagName() + "[" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']") )) {
                    continue;
                }
            }

//            final Node newNode = nodes.add( ctxt.currentTagName() + "[" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']", true);
//
//            final Set<Path> paths = new NodeVisitor().visit(newNode);
//            System.out.println("::: IV: " + paths.size() + " paths: " + paths);
//
//            for (Path each : paths) {
//                if (tester.ok( By.cssSelector( each.getPath() ) )) {
//                    return true;
//                }
//            }
        }

        return false;
    }
}