package org.hiatusuk.selectorLint.handlers;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.hiatusuk.selectorLint.ElementContext;
import org.hiatusuk.selectorLint.MatchTester;
import org.hiatusuk.selectorLint.NodeAdder;
import org.hiatusuk.selectorLint.Options.Rules;
import org.hiatusuk.selectorLint.RulesBasedFilter;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
import org.hiatusuk.selectorLint.tree.Path;
import org.openqa.selenium.By;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class AttributesHandler extends AbstractBaseHandler {

    private final Predicate<String> ignoreKeys;
    private final Predicate<String> keysNeedSemanticValue;

    public AttributesHandler(final Rules rules, final List<String> ignoreAttributes, final List<String> keysNeedSemanticValue) {
        this.ignoreKeys = Predicates.in(ignoreAttributes); // new RulesBasedFilter(rules, ignoreAttributes);
        this.keysNeedSemanticValue = Predicates.in(keysNeedSemanticValue); // new RulesBasedFilter(rules, keysNeedSemanticValue);
    }

    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {

        for (Entry<String, String> eachAttr : ctxt.attributes().entrySet()) {
            if (ignoreKeys.apply(eachAttr.getKey())) {
                continue;
            }

            System.out.println(">>> TRY: " + ctxt.currentTagName() + "." + eachAttr.getKey());

            if (keysNeedSemanticValue.apply(ctxt.currentTagName() + "." + eachAttr.getKey()) /* && Semantic.isNonSemantic( inEntry.getValue() ) */ ) {
                // System.out.println("=> Attr FALSE for " + inEntry.getKey() + " / " + inEntry.getValue());
                continue;
            }
            
            if (keysNeedSemanticValue.apply(eachAttr.getKey()) /* && Semantic.isNonSemantic( inEntry.getValue() ) */ ) {
                // System.out.println("=> Attr FALSE for " + inEntry.getKey() + " / " + inEntry.getValue());
                continue;
            }
            
//            if (eachGoodAttr.getValue().isEmpty()) {
//                continue;
//            }
//            if (ctxt.currentTagName().equals("input") && eachGoodAttr.getKey().equals("value")) {
//                continue;  // ignore these
//            }

            ctxt.setHasSomeProps();

            if (ctxt.isLeaf()) {
                // By trying = By.xpath(".//*[@" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                if (tester.ok( By.cssSelector( ctxt.currentTagName() + "[" + eachAttr.getKey() + "='" + eachAttr.getValue() + "']") )) {
                    continue;
                }
            }

            final Node newNode = nodes.add( ctxt.currentTagName() + "[" + eachAttr.getKey() + "='" + eachAttr.getValue() + "']", true);

            final Set<Path> paths = new NodeVisitor().visit(newNode);
            // System.out.println("::: ATTR: " + paths.size() + " paths: " + paths);

            for (Path each : paths) {
                if (tester.ok( By.cssSelector( each.getPath() ) )) {
                    return true;
                }
            }
        }

        return false;
    }
}