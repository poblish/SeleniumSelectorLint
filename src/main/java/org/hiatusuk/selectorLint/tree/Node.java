package org.hiatusuk.selectorLint.tree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Node implements Iterable<NodeRelation> {
    private Set<NodeRelation> children = new HashSet<>();
    private String self;

    public Node(String self) {
        this.self = self;
    }

    public void addChild( Node node, boolean direct) {
        Preconditions.checkArgument(node != this);
        children.add( new NodeRelation(node, direct) );
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public CharSequence getSelector() {
        return this.self;
    }

    @Override
    public Iterator<NodeRelation> iterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("selector", self).add("children", children).omitNullValues().toString();
    }
}
