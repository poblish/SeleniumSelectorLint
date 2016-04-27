package org.hiatusuk.selectorLint.handlers;

import org.hiatusuk.selectorLint.ElementContext;
import org.hiatusuk.selectorLint.MatchTester;
import org.hiatusuk.selectorLint.NodeAdder;

public interface ElementHandler {

    boolean getImprovedSelectors(final ElementContext ctxt, final NodeAdder nodes, final MatchTester tester);
}
