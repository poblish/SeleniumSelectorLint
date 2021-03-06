package org.hiatusuk.selectorLint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Set;

import org.hiatusuk.eqhash.EqHash;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
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

        Node n_6_a = new Node("#dashboard");
        n_6_a.addChild(n_5, /* direct */ true);
        n_6_a.addChild(n_4, false);
        testPaths(n_6_a, "[Path{score=3, path=#dashboard div:nth-child(10) div.other}, Path{score=3, path=#dashboard div:nth-child(10) div.title}, Path{score=4, path=#dashboard > div > div:nth-child(10) div.other}, Path{score=4, path=#dashboard > div > div:nth-child(10) div.title}, Path{score=5, path=#dashboard div:nth-child(10) > div > div > div.other}, Path{score=5, path=#dashboard div:nth-child(10) > div > div > div.title}, Path{score=6, path=#dashboard > div > div:nth-child(10) > div > div > div.other}, Path{score=6, path=#dashboard > div > div:nth-child(10) > div > div > div.title}]");
        Node n_6_b = new Node("article[attr='goodValue']");
        n_6_b.addChild(n_5, /* direct */ true);
        n_6_b.addChild(n_4, false);
        testPaths(n_6_b, "[Path{score=3, path=article[attr='goodValue'] div:nth-child(10) div.other}, Path{score=3, path=article[attr='goodValue'] div:nth-child(10) div.title}, Path{score=4, path=article[attr='goodValue'] > div > div:nth-child(10) div.other}, Path{score=4, path=article[attr='goodValue'] > div > div:nth-child(10) div.title}, Path{score=5, path=article[attr='goodValue'] div:nth-child(10) > div > div > div.other}, Path{score=5, path=article[attr='goodValue'] div:nth-child(10) > div > div > div.title}, Path{score=6, path=article[attr='goodValue'] > div > div:nth-child(10) > div > div > div.other}, Path{score=6, path=article[attr='goodValue'] > div > div:nth-child(10) > div > div > div.title}]");
    }

    @Test public void testEqualsHash() {
        Node n = new Node("div.title");
        Node copy = new Node("div.title");
        Node diff1 = new Node("div.title").addChild(n, true);
        Node diff2 = new Node("div.xxx").addChild(n, true);
        EqHash.testEqualsHashcode(n, copy, diff1, diff2);
    }

    @Test public void testSimpleRelation() {
        final Node n1 = new Node("div");
        final Node n2 = new Node("span");
        n1.addChild(n2, true);
        assertThat( n1.iterator().next().toString(), is("Relation{target=Node{selector=span, children=[]}, direct?=true}"));
        assertThat( n2.iterator().hasNext(), is(false));
    }

    @Test public void testPathsEqualsHash() {
        Path p = new Path().append("div.title", true);
        Path p2 = new Path().append("div.title", true);
        Path diff1 = new Path().append("div.xxx", true);
        Path diff2 = new Path().append("div.xxx", true).append("span", true);
        EqHash.testEqualsHashcode(p, p2, diff1, diff2);
    }

    private void testPaths(Node top, String exp) {
        Set<Path> paths = new NodeVisitor().visit(top, /* override max depth for tests */ 999);
        System.out.println( paths.size() + " paths: " + paths);
        assertThat( paths.toString(), is(exp));
    }
}