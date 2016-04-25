package org.hiatusuk.selectorLint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeRelation;
import org.hiatusuk.selectorLint.tree.Path;
import org.testng.annotations.Test;

public class StackToSelectorTest {

    @Test public void testNodes() {
        Node n_original_a = new Node("div.title");
        Node n_original_b = new Node("div.other");
        testPaths(n_original_a, "[Path{score=1, path=div.title}]");
        testPaths(n_original_b, "[Path{score=1, path=div.other}]");

        Node n_2 = new Node("div");
        n_2.addChild(n_original_a, /* direct */ true);
        n_2.addChild(n_original_b, /* direct */ true);
        testPaths(n_2, "[Path{score=2, path=div > div.other}, Path{score=2, path=div > div.title}]");

        Node n_3 = new Node("div");
        n_3.addChild(n_2, /* direct */ true);
        testPaths(n_3, "[Path{score=3, path=div > div > div.other}, Path{score=3, path=div > div > div.title}]");

        Node n_4 = new Node("div:nth-child(10)");
        n_4.addChild(n_3, /* direct */ true);
        n_4.addChild(n_original_a, false);
        n_4.addChild(n_original_b, false);
        testPaths(n_4, "[Path{score=2, path=div:nth-child(10) div.other}, Path{score=2, path=div:nth-child(10) div.title}, Path{score=4, path=div:nth-child(10) > div > div > div.other}, Path{score=4, path=div:nth-child(10) > div > div > div.title}]");

        Node n_5 = new Node("div");
        n_5.addChild(n_4, /* direct */ true);
        testPaths(n_5, "[Path{score=3, path=div > div:nth-child(10) div.other}, Path{score=3, path=div > div:nth-child(10) div.title}, Path{score=5, path=div > div:nth-child(10) > div > div > div.other}, Path{score=5, path=div > div:nth-child(10) > div > div > div.title}]");

        Node n_6 = new Node("#dashboard");
        n_6.addChild(n_5, /* direct */ true);
        n_6.addChild(n_4, false);
        testPaths(n_6, "[Path{score=3, path=#dashboard div:nth-child(10) div.other}, Path{score=3, path=#dashboard div:nth-child(10) div.title}, Path{score=4, path=#dashboard > div > div:nth-child(10) div.other}, Path{score=4, path=#dashboard > div > div:nth-child(10) div.title}, Path{score=5, path=#dashboard div:nth-child(10) > div > div > div.other}, Path{score=5, path=#dashboard div:nth-child(10) > div > div > div.title}, Path{score=6, path=#dashboard > div > div:nth-child(10) > div > div > div.other}, Path{score=6, path=#dashboard > div > div:nth-child(10) > div > div > div.title}]");
    }

    private void testPaths(Node top, String exp) {
        Set<Path> paths = new TreeSet<>();
        visitNodes(top, /* irrelevant */ false, paths, new Path());
        System.out.println( paths.size() + " paths: " + paths);
        assertThat( paths.toString(), is(exp));
    }

    private void visitNodes(final Node currNode, boolean isDirect, Collection<Path> paths, Path currPath) {
        currPath.append(currNode.getSelector(), isDirect);

        if (currNode.isLeaf()) {
            paths.add(currPath);
            return;
        }

        for (final NodeRelation each : currNode) {
            visitNodes(each.getTarget(), each.isDirectParent(), paths, new Path(currPath));
        }
    }
}