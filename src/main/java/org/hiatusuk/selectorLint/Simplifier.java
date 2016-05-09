package org.hiatusuk.selectorLint;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hiatusuk.selectorLint.handlers.ElementHandler;
import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.utils.Strings;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class Simplifier {

    private static final Predicate<String> CAN_USE_TEXT_TAGS = Predicates.in(Arrays.asList("option","td","th","var"));

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
                List<WebElement> tried = driver.findElements(selector);
                return tried.equals(originals);
            }
        };

        final NodeAdder nodes = new NodeAdder() {

            @Override
            public Node add( String selector, boolean direct) {
                return Simplifier.this.addNode(selector, direct);
            }};

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
                    return results;
                }
            }
    
            // Try XPath text() matches
            if (results.isEmpty() && Strings.hasString( currentElement.getText() )) { // Try text
                if (CAN_USE_TEXT_TAGS.apply( tagName )) {  // Anything else?!?
                    hasSomeProps = true;

                    tester.ok( By.xpath("//" + tagName + "[text()='" + currentElement.getText() + "']") );
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
                    List<WebElement> precedingSibs = currentElement.findElements(By.xpath("preceding-sibling::" + tagName));
                    List<WebElement> followingSibs = currentElement.findElements(By.xpath("following-sibling::" + tagName));
    
                    if (precedingSibs.isEmpty() && followingSibs.isEmpty()) {
                        addNode( tagName, isLeaf || hasSomeProps);
                    }
                    else {
                        addNode( tagName + ":nth-child(" + (precedingSibs.size() + 1) + ")", true);
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
}
