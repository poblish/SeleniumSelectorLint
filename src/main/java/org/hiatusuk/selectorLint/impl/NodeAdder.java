package org.hiatusuk.selectorLint.impl;

import org.hiatusuk.selectorLint.tree.Node;

public interface NodeAdder {
    Node add(final String selector, final boolean direct);
}
