package org.hiatusuk.selectorLint.handlers;

import static com.google.common.base.Predicates.in;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hiatusuk.selectorLint.ElementContext;
import org.hiatusuk.selectorLint.MatchTester;
import org.hiatusuk.selectorLint.NodeAdder;
import org.hiatusuk.selectorLint.Semantic;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
import org.hiatusuk.selectorLint.tree.Path;
import org.openqa.selenium.By;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class AttributesHandler extends AbstractBaseHandler {

    private final Predicate<String> ignoreKeys;
    private final Predicate<String> keysNeedSemanticValue;

    private final Predicate<Entry<String,String>> acceptRule = new Predicate<Entry<String,String>>() {

        @Override
        public boolean apply(final Entry<String,String> inEntry) {
            if (ignoreKeys.apply(inEntry.getKey())) {
                return false;
            }

            if (keysNeedSemanticValue.apply(inEntry.getKey()) && Semantic.isNonSemantic( inEntry.getValue() )) {
                return false;
            }

            return true;
        }};

    public AttributesHandler(final List<String> ignoreAttributes, final List<String> keysNeedSemanticValue) {
        this.ignoreKeys = in(ignoreAttributes);
        this.keysNeedSemanticValue = in(keysNeedSemanticValue);
    }

    private Map<String,String> filterQuality(final Map<String,String> attrs) {
        return Maps.filterEntries(attrs, acceptRule);
    }

    public boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester) {

        for (Entry<String, String> eachGoodAttr : filterQuality( ctxt.attributes() ).entrySet()) {
            if (ctxt.currentTagName().equals("input") && eachGoodAttr.getKey().equals("value") && Semantic.isNonSemantic( eachGoodAttr.getValue() )) {
                continue;  // non-semantic
            }

            ctxt.setHasSomeProps();

            if (ctxt.isLeaf()) {
                // By trying = By.xpath(".//*[@" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                if (tester.ok( By.cssSelector( ctxt.currentTagName() + "[" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']") )) {
                    continue;
                }
            }

            final Node newNode = nodes.add( ctxt.currentTagName() + "[" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']", true);

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