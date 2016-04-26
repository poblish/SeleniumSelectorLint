package org.hiatusuk.selectorLint;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.*;
import java.util.Map.Entry;

import org.hiatusuk.selectorLint.tree.Node;
import org.hiatusuk.selectorLint.tree.NodeVisitor;
import org.hiatusuk.selectorLint.tree.Path;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class Simplifier {

    private static final Predicate<String> CAN_USE_TEXT_TAGS = Predicates.in(Arrays.asList("option","td","var"));

    private final WebDriver driver;
    private final List<By> results = new ArrayList<>();
    private List<Node> currentLevelSelectors;
    private List<Node> childLevelSelectors;

    /* FIXME */ private boolean hasSomeProps;
    /* FIXME */ private boolean addedGoodNonUniqueNode;

    private String tagName;
    boolean isLeaf;
    private Node lastPivotNode;

    public Simplifier(final WebDriver inDriver) {
        driver = checkNotNull(inDriver);
    }

    public List<By> getImprovedSelector( final WebElement original, final String originalSelectorString) {

        WebElement current = original;

        results.clear();
        currentLevelSelectors = new ArrayList<>();
        childLevelSelectors = new ArrayList<>();

        isLeaf = true;
        lastPivotNode = null;

        while (true) {
            System.out.println("> current = " + current + ", lastPivot = " + lastPivotNode + ", currSels = " + currentLevelSelectors);

            tagName = current.getTagName();
    
            final Map<String,String> attrs = attributes(driver, current);
 
            hasSomeProps = addedGoodNonUniqueNode = false;

            if (handleTags(current, original, originalSelectorString)) {
                return results;
            }

            if (handleIds(current, attrs, original, originalSelectorString)) {
                return results;
            }

            if (handleClasses(current, attrs, original, originalSelectorString)) {
                return results;
            }

            if (handleAttributes(current, attrs, original, originalSelectorString)) {
                return results;
            }
    
            // Try XPath text() matches
            if (results.isEmpty() && hasString( current.getText() )) { // Try text
                if (CAN_USE_TEXT_TAGS.apply( tagName )) {  // Anything else?!?
                    hasSomeProps = true;

                    By trying = By.xpath("//" + tagName + "[text()='" + current.getText() + "']");
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

            final WebElement parent = current.findElement(By.xpath(".."));

            if (!addedGoodNonUniqueNode) {
                List<WebElement> precedingSibs = current.findElements(By.xpath("preceding-sibling::" + tagName));
                List<WebElement> followingSibs = current.findElements(By.xpath("following-sibling::" + tagName));

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

            current = parent;
            isLeaf = false;
        }

        // Now it gets more complex - what can we do to normalise...? Either parse and fix original selector or pick a much simpler, purer one?
        // So far, no Id, class, or attr has *uniquely* referenced us...
        // But we surely HAVE to check for some intrinsic attrs, otherwise we very likely will have to use nth-child or bail out.
    }

    private boolean handleTags(final WebElement current, final WebElement original, final String originalSelectorString) {
        if (Tags.isGoodQuality(current)) {
            if (isLeaf) {
                By trying = By.tagName(tagName);

                if (originalSelectorString.equals( trying.toString() ) ||
                    originalSelectorString.equalsIgnoreCase("By.cssSelector: " + tagName) ||
                    originalSelectorString.equalsIgnoreCase("By.tagName: " + tagName)) {
                    // If CSS 'body' was passed in, don't propose pointlessly changing to tagName syntax
                    return true;
                }
            
                if (isUnique(trying, original)) {
                    results.add(trying);
                    /* FIXME */ return true;
                }
            }

            final Node newNode = addNode( tagName, true);

            hasSomeProps = addedGoodNonUniqueNode = true;

            Set<Path> paths = new NodeVisitor().visit(newNode);
            System.out.println("::: TAG: " + paths.size() + " paths: " + paths);

            for (Path each : paths) {
                By trying2 = By.cssSelector( each.getPath() );
                if (isUnique(trying2, original)) {
                    results.add(trying2);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean handleIds(final WebElement current, Map<String,String> attrs, final WebElement original, final String originalSelectorString) {
        final String id = attrs.get("id");
        // Need a quality/generated check on this Id!
        if (hasString(id) && !Ids.isGeneratedString(id)) {
            final String strVersion = originalSelectorString;
            if (strVersion.equals("By.id: " + id) ||
                strVersion.equals("By.cssSelector: #" + id) ||
                strVersion.equals("By.xpath: //*[@id=\"" + id + "\"]") ||
                strVersion.equals("By.cssSelector: " + tagName + "#" + id) ||
                strVersion.equals("By.xpath: //" + tagName + "[@id=\"" + id + "\"]")) {
                // Potential Id query identical to existing CSS query, so skip
                return true;
            }

            if (isLeaf) {
                By trying = By.id(id);
//                if (originalSelectorString.equals( trying.toString() )) {
//                    return true;
//                }

                if (isUnique(trying, original)) {
                    results.add(trying);
                    return true;
                }
            }

            final Node newNode = addNode("#" + id, true);

            Set<Path> paths = new NodeVisitor().visit(newNode);
            System.out.println("::: " + paths.size() + " paths: " + paths);

            for (Path each : paths) {
                By trying2 = By.cssSelector( each.getPath() );
                if (originalSelectorString.equals( trying2.toString() )) {
                    return true;
                }
                if (isUnique(trying2, original)) {
                    results.add(trying2);
                    return true;
                }
            }

            hasSomeProps = true;
            addedGoodNonUniqueNode = true;
        }

        return false;
    }

    private boolean handleClasses(final WebElement current, Map<String,String> attrs, final WebElement original, final String originalSelectorString) {
        // Need to filter, or at least *score* these!
        for (String eachClass : Classes.filter( attrs.get("class") )) {  // Will never be empty strings
            hasSomeProps = true;

            if (isLeaf) {
                By trying = By.cssSelector(tagName + "." + CssUtils.cssEscape(eachClass));  // Best of all: more testable than By.className, handles multiple classnames 
                if (isUnique(trying, original)) {
                    results.add(trying);
                    continue;
                }
            }

            final Node newNode = addNode( tagName + "." + CssUtils.cssEscape(eachClass), true);

            Set<Path> paths = new NodeVisitor().visit(newNode);
            System.out.println("::: CLASS: " + paths.size() + " paths: " + paths);

            for (Path each : paths) {
                By trying2 = By.cssSelector( each.getPath() );
                if (isUnique(trying2, original)) {
                    results.add(trying2);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean handleAttributes(final WebElement current, Map<String,String> attrs, final WebElement original, final String originalSelectorString) {
        // Need to filter attrs!!!
        for (Entry<String, String> eachGoodAttr : Attributes.filterQuality(attrs).entrySet()) {
            if (tagName.equals("input") && eachGoodAttr.getKey().equals("value") && Attributes.isNonSemantic( eachGoodAttr.getValue() )) {
                continue;  // non-semantic
            }

            hasSomeProps = true;

            if (isLeaf) {
                // By trying = By.xpath(".//*[@" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                By trying = By.cssSelector(tagName + "[" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                if (isUnique(trying, original)) {
                    results.add(trying);
                    continue;
                }
            }

            final Node newNode = addNode( tagName + "[" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']", true);

            Set<Path> paths = new NodeVisitor().visit(newNode);
            System.out.println("::: ATTR: " + paths.size() + " paths: " + paths);

            for (Path each : paths) {
                By trying2 = By.cssSelector( each.getPath() );
                if (isUnique(trying2, original)) {
                    results.add(trying2);
                    return true;
                }
            }
        }

        return false;
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

    private static boolean hasString( String val) {
        return val != null && !val.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> attributes( WebDriver driver, WebElement elem) {
        final JavascriptExecutor js = (JavascriptExecutor) driver;
        // *Much* much more efficient than keep calling element.attribute()
        return (Map<String, String>) js.executeScript("var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index) { items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;", elem);
    }

    private boolean isUnique(final By selector, final WebElement original) {
        System.out.println(",,,, " + selector);
        List<WebElement> tried = driver.findElements(selector);
        return (tried.size() == 1 && original.equals(tried.get(0)));
    }
}
