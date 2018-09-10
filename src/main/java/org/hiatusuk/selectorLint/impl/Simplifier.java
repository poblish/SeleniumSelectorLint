package org.hiatusuk.selectorLint.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.hiatusuk.selectorLint.ElementContext;
import org.hiatusuk.selectorLint.config.Options;
import org.hiatusuk.selectorLint.handlers.ElementHandler;
import org.hiatusuk.selectorLint.handlers.IdsHandler;
import org.hiatusuk.selectorLint.tree.Node;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class Simplifier {

    private Options options;
    private boolean convertCss = false;

    private final WebDriver driver;
    private final List<By> results = new ArrayList<>();
    private List<Node> currentLevelSelectors;
    private List<Node> childLevelSelectors;

    /* FIXME */ private boolean hasSomeProps;
    /* FIXME */ private boolean addedGoodNonUniqueNode;
    private boolean skippedUselessElement;
    private boolean lastPivotSetForThisLevel;

    private WebElement currentElement;
    private String tagName;
    private Map<String,String> elementAttrs;
    private boolean isLeaf;
    private Node lastPivotNode;

    private final Cache<By,Boolean> selectorCacheByPage = CacheBuilder.newBuilder().build();


    public Simplifier(final WebDriver inDriver) {
        driver = checkNotNull(inDriver);
    }

    public void setOptions(final Options opts) {
        options = checkNotNull(opts);
    }

    public void convertCssToXPath(final boolean convertCss) {
        this.convertCss = convertCss;
    }

    public List<By> getImprovedSelector( final List<WebElement> originalMatches, final String originalSelectorString) {

        /////////////////////////////////////////////////////////////////

        final ElementContext ctxt = new ElementContext() {

            @Override
            public String getOriginalSelector() {
                return originalSelectorString;
            }

            @Override
            public boolean isLeaf() {
                return Simplifier.this.isLeaf;
            }

            @Override
            public String currentTagName() {
                return Simplifier.this.tagName;
            }

            @Override
            public Map<String, String> attributes() {
                return Simplifier.this.elementAttrs;
            }

            @Override
            public void setHasSomeProps() {
                Simplifier.this.hasSomeProps = true;
            }

            @Override
            public void setAddedGoodNonUniqueNode() {
                Simplifier.this.addedGoodNonUniqueNode = true;
            }

            @Override
            public WebElement element() {
                return Simplifier.this.currentElement;
            }

            @Override
            public boolean skippedUselessElement() {
                return Simplifier.this.skippedUselessElement;
            }
        };

        final MatchTester tester = new MatchTester() {

            @Override
            public boolean ok(final By selector) {
                if (isUnique(selector, originalMatches)) {
                    results.add(selector);
                    return true;
                }
                return false;
            }

            private boolean isUnique(final By selector, final List<WebElement> originals) {
                try {
                    return selectorCacheByPage.get(selector, () -> {
                        System.out.println("... " + selector);
                        return driver.findElements(selector).equals(originals);
                    });
                }
                catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        final NodeAdder nodes = Simplifier.this::addNode;

        /////////////////////////////////////////////////////////////////

        currentElement = originalMatches.get(0);  // FIXME Need to check more than one!

        results.clear();

        currentLevelSelectors = new ArrayList<>();
        childLevelSelectors = new ArrayList<>();

        isLeaf = true;
        lastPivotNode = null;

        while (true) {
            // System.out.println("> current = " + currentElement + ", lastPivot = " + lastPivotNode + ", currSels = " + currentLevelSelectors);

            tagName = currentElement.getTagName();
            elementAttrs = attributes(driver, currentElement);
 
            hasSomeProps = addedGoodNonUniqueNode = lastPivotSetForThisLevel = false;

            for (ElementHandler eachHandler : options.handlers()) {
                if (eachHandler.getImprovedSelectors(ctxt, nodes, tester)) {
                    if (/* FIXME Pretty vile assumption */ eachHandler instanceof IdsHandler) {
                        return results;
                    }
                }
            }

            if (!results.isEmpty()) {  // Is this premature?!?
                return results;
            }

            if (tagName.equals("html")) {
                return results;
            }

            skippedUselessElement = false;

            for (ElementHandler eachHandler : options.handlers()) {
                if (eachHandler.shouldSkip(tagName)) {
                    skippedUselessElement = true;
                    break;
                }
            }

            if (!skippedUselessElement) {
                if (!addedGoodNonUniqueNode) {
                    // See if we're the only instance of our Tag under this parent, i.e. we don't need nth-child at all
                    if (currentElement.findElements(By.xpath("preceding-sibling::" + tagName)).isEmpty() &&
                        currentElement.findElements(By.xpath("following-sibling::" + tagName)).isEmpty()) {
                        addNode( tagName, isLeaf || hasSomeProps);
                    }
                    else {
                        // We're not on our own, so we need to know our *actual* position among all the children
                        final List<WebElement> precedingSibsOfAll = currentElement.findElements(By.xpath("preceding-sibling::*"));
                        addNode( tagName + ":nth-child(" + (precedingSibsOfAll.size() + 1) + ")", true);
                    }
                }

                // System.out.println("** ITERATE curr ==> " + currentLevelSelectors);
                // System.out.println("**         prev ==> " + childLevelSelectors);
    
                childLevelSelectors = currentLevelSelectors;
                currentLevelSelectors = new ArrayList<>();
            }

            currentElement = currentElement.findElement(By.xpath(".."));  // up a level
            isLeaf = false;
        }

        // Now it gets more complex - what can we do to normalise...? Either parse and fix original selector or pick a much simpler, purer one?
        // So far, no Id, class, or attr has *uniquely* referenced us...
        // But we surely HAVE to check for some intrinsic attrs, otherwise we very likely will have to use nth-child or bail out.
    }

    private Node addNode(final String selector, final boolean direct) {
        final Node n = new Node(selector);
        currentLevelSelectors.add(n);

        for (Node eachChild : childLevelSelectors) {
            n.addChild(eachChild, direct);
        }

        if (lastPivotNode == null) {
            lastPivotNode = n;
            lastPivotSetForThisLevel = true;
        }
        else if (!lastPivotSetForThisLevel) {
            // Don't add to a pivot we've only just set, i.e. for current element!
            n.addChild(lastPivotNode, false);
        }

        return n;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> attributes( WebDriver driver, WebElement elem) {
        final JavascriptExecutor js = (JavascriptExecutor) driver;
        // *Much* much more efficient than keep calling element.attribute()
        return (Map<String, String>) js.executeScript("var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index) { items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;", elem);
    }

    public void clearCache() {
        selectorCacheByPage.invalidateAll();
    }
}
