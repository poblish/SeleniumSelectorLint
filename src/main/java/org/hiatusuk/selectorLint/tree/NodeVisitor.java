package org.hiatusuk.selectorLint.tree;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class NodeVisitor {
    private final Set<Path> paths = new TreeSet<>();

    public Set<Path> visit(final Node top) {
        return visit(top, 4);
    }

    public Set<Path> visit(final Node top, final int maxDepth) {
        doVisit(top, /* irrelevant */ false, paths, new Path(), maxDepth);
        return paths;
    }

    private void doVisit(final Node currNode, boolean isDirect, Collection<Path> paths, Path currPath, final int maxDepth) {
        currPath.append(currNode.getSelector(), isDirect);

        if (currNode.isLeaf()) {
            if (currPath.getScore() > maxDepth) {
                // Too long - skip!
                return;
            }

            // Forbid paths with multiple nth-child
            final int idx = currPath.getPath().indexOf("nth-child");
            if (idx > 0 && ( currPath.getPath().indexOf("nth-child", idx + 1)) > 0) {
                return;
            }

            paths.add(currPath);
        }

        for (final NodeRelation each : currNode) {
            doVisit(each.getTarget(), each.isDirectParent(), paths, new Path(currPath), maxDepth);
        }
    }

}
