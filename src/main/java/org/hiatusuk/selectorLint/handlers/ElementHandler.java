package org.hiatusuk.selectorLint.handlers;

import org.hiatusuk.selectorLint.ElementContext;
import org.hiatusuk.selectorLint.impl.MatchTester;
import org.hiatusuk.selectorLint.impl.NodeAdder;

public interface ElementHandler {

    boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester);

    boolean shouldSkip( String tagName);  // Ugly, FIXME
}
