package org.hiatusuk.selectorLint;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hiatusuk.selectorLint.handlers.*;
import org.hiatusuk.selectorLint.tree.Node;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class Simplifier {

    private static final Predicate<String> CAN_USE_TEXT_TAGS = Predicates.in(Arrays.asList("option","td","var"));

    private final ElementHandler tags;
    private final ElementHandler ids;
    private final ElementHandler classes;
    private final ElementHandler attrsHandler;

    private final WebDriver driver;
    private final List<By> results = new ArrayList<>();
    private List<Node> currentLevelSelectors;
    private List<Node> childLevelSelectors;

    /* FIXME */ private boolean hasSomeProps;
    /* FIXME */ private boolean addedGoodNonUniqueNode;
    private boolean skippedUselessElement;

    private WebElement currentElement;
    private String tagName;
    private Map<String,String> elementAttrs;
    private boolean isLeaf;
    private Node lastPivotNode;

    public Simplifier(final WebDriver inDriver) {
        driver = checkNotNull(inDriver);

        tags = new TagHandler("article","body","footer","head","table","h1");
        ids = new IdsHandler();
        classes = new ClassHandler( Arrays.asList("clear-fix","bold","blue"), /* Min: */ 3);
        attrsHandler = new AttributesHandler( Arrays.asList("class","id","disabled","style","gh","cellpadding","tabindex","lang","onclick"), Arrays.asList("aria-labelledby"));
    }

    public List<By> getImprovedSelector( final WebElement original, final String originalSelectorString) {

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
                if (isUnique(selector, original)) {
                    results.add(selector);
                    return true;
                }
                return false;
            }};

        final NodeAdder nodes = new NodeAdder() {

            @Override
            public Node add( String selector, boolean direct) {
                return Simplifier.this.addNode(selector, direct);
            }};

        /////////////////////////////////////////////////////////////////

        currentElement = original;

        results.clear();

        currentLevelSelectors = new ArrayList<>();
        childLevelSelectors = new ArrayList<>();

        isLeaf = true;
        lastPivotNode = null;

        while (true) {
            System.out.println("> current = " + currentElement + ", lastPivot = " + lastPivotNode + ", currSels = " + currentLevelSelectors);

            tagName = currentElement.getTagName();
            elementAttrs = attributes(driver, currentElement);
 
            hasSomeProps = addedGoodNonUniqueNode = false;

            if (tags.getImprovedSelectors(ctxt, nodes, tester)) {
                return results;
            }

            if (ids.getImprovedSelectors(ctxt, nodes, tester)) {
                return results;
            }

            if (classes.getImprovedSelectors(ctxt, nodes, tester)) {
                return results;
            }

            if (attrsHandler.getImprovedSelectors(ctxt, nodes, tester)) {
                return results;
            }
    
            // Try XPath text() matches
            if (results.isEmpty() && Strings.hasString( currentElement.getText() )) { // Try text
                if (CAN_USE_TEXT_TAGS.apply( tagName )) {  // Anything else?!?
                    hasSomeProps = true;

                    By trying = By.xpath("//" + tagName + "[text()='" + currentElement.getText() + "']");
                    if (isUnique(trying, original)) {
                        results.add(trying);
                    }
                }
            }
    
            if (!results.isEmpty()) {  // Is this premature?!?
                return results;
            }

            if (tagName.equals("html")) {
                return results;
            }

            final WebElement parent = currentElement.findElement(By.xpath(".."));

            if (tagName.equals("tbody")) {
                skippedUselessElement = true;
            }
            else {
                skippedUselessElement = false;

                if (!addedGoodNonUniqueNode) {
                    List<WebElement> precedingSibs = currentElement.findElements(By.xpath("preceding-sibling::" + tagName));
                    List<WebElement> followingSibs = currentElement.findElements(By.xpath("following-sibling::" + tagName));
    
                    if (precedingSibs.isEmpty() && followingSibs.isEmpty()) {
                        addNode( tagName, isLeaf || hasSomeProps);
                    }
                    else {
                        addNode( tagName + ":nth-child(" + (precedingSibs.size() + 1) + ")", false);
                    }
                }

                // System.out.println("** ITERATE curr ==> " + currentLevelSelectors);
                // System.out.println("**         prev ==> " + childLevelSelectors);
    
                childLevelSelectors = currentLevelSelectors;
                currentLevelSelectors = new ArrayList<>();
            }

            currentElement = parent;
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
        }
        else {
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

    private boolean isUnique(final By selector, final WebElement original) {
        List<WebElement> tried = driver.findElements(selector);
        return (tried.size() == 1 && original.equals(tried.get(0)));
    }
}
