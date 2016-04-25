package org.hiatusuk.selectorLint.tree;

import com.google.common.base.MoreObjects;

public class NodeRelation {
    private boolean directParent;
    private Node target;

    public NodeRelation(final Node node, final boolean direct) {
        this.target = node;
        this.directParent = direct;
    }

    public Node getTarget() {
        return target;
    }

    public boolean isDirectParent() {
        return directParent;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Relation").add("target", target).add("direct?", directParent).omitNullValues().toString();
    }
}
