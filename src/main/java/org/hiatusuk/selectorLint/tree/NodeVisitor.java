package org.hiatusuk.selectorLint.tree;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class NodeVisitor {
    private final Set<Path> paths = new TreeSet<>();

    public Set<Path> visit(final Node top) {
        doVisit(top, /* irrelevant */ false, paths, new Path());
        return paths;
    }

    private void doVisit(final Node currNode, boolean isDirect, Collection<Path> paths, Path currPath) {
        currPath.append(currNode.getSelector(), isDirect);

        if (currNode.isLeaf()) {
            paths.add(currPath);
            return;
        }

        for (final NodeRelation each : currNode) {
            doVisit(each.getTarget(), each.isDirectParent(), paths, new Path(currPath));
        }
    }

}
