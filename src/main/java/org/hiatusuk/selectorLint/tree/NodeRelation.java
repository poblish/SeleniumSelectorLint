package org.hiatusuk.selectorLint.tree;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class NodeRelation {
    private final boolean directParent;
    private final Node target;

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
    public int hashCode() {
        return Objects.hash(directParent, target);
    }

    @Override
    public boolean equals( Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NodeRelation)) {
            return false;
        }
        final NodeRelation other = (NodeRelation) obj;
        return directParent == other.directParent && Objects.equals(target, other.target);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Relation").add("target", target).add("direct?", directParent).omitNullValues().toString();
    }
}
