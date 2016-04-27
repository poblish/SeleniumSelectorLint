package org.hiatusuk.selectorLint;

import org.hiatusuk.selectorLint.tree.Node;

public interface NodeAdder {
    Node add(final String selector, final boolean direct);
}
