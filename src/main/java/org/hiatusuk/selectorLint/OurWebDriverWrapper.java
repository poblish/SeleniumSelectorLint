package org.hiatusuk.selectorLint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class OurWebDriverWrapper extends WebDriverWrapper {

    private static final Predicate<String> CAN_USE_TEXT_TAGS = Predicates.in(Arrays.asList("option","td","var"));

    public OurWebDriverWrapper(WebDriver originalDriver) {
        super(originalDriver);
    }

    public List<By> getImprovedSelector( final WebElement original, final By originalSelector) {
//        if (original == null) {
//            System.out.println("Cannot improve upon " + originalSelector);
//            return null;
//        }

        final List<By> results = new ArrayList<>();
        final String tagName = original.getTagName();

        if (originalSelector instanceof By.ById) {
            final Map<String, String> attrs = attributes(getWrappedDriver(), original);

            final String id = attrs.get("id");  // must be set!!
            if (!Ids.isGeneratedString(id)) {
                return results;  // fine as we are!
            }

            // Supplied Id is no good. Fall back to try class, attrs etc.
            attrs.remove("id");

            // FIXME Copy/paste from CSS selector below!
            for (Entry<String, String> eachGoodAttr : attrs.entrySet()) {
                // By trying = By.xpath(".//*[@" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                By trying = By.cssSelector(tagName + "[" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                // System.out.println("+++ Trying... " + trying);
                if (isUnique(trying, original)) {
                    results.add(trying);
                }
            }
        }

        if (originalSelector instanceof By.ByCssSelector || originalSelector instanceof By.ByXPath) {
            final Map<String, String> attrs = attributes(getWrappedDriver(), original);

            final String id = attrs.get("id");
            // Need a quality/generated check on this Id!
            if (hasString(id) && !Ids.isGeneratedString(id)) {
                final String strVersion = originalSelector.toString();
                if (strVersion.equals("By.cssSelector: #" + id) ||
                    strVersion.equals("By.xpath: //*[@id=\"" + id + "\"]") ||
                    strVersion.equals("By.cssSelector: " + tagName + "#" + id) ||
                    strVersion.equals("By.xpath: //" + tagName + "[@id=\"" + id + "\"]")) {
                    // Potential Id query identical to existing CSS query, so skip
                }
                else {
                    By trying = By.id(id);
                    if (isUnique(trying, original)) {
                        results.add(trying);
                    }
                }
                return results;
            }

            if (Tags.isGoodQuality(original)) {
                By trying = By.tagName(tagName);
                List<WebElement> tried = findElements(trying);
                if (tried.size() == 1 && original.equals(tried.get(0))) {

                    if (originalSelector.toString().equalsIgnoreCase("By.cssSelector: " + tagName)) {
                        // If CSS 'body' was passed in, don't propose pointlessly changing to tagName syntax
                        return results;
                    }

//                    System.out.println("+++ Try ." + trying);
                    results.add(trying);
                    return results;
                }
            }

            // Need to filter, or at least *score* these!
            String[] classes = Classes.filter( attrs.get("class") );
            for (String eachClass : classes) {
                if (eachClass.isEmpty()) {
                    continue;
                }
                By trying = By.className(eachClass);
                if (isUnique(trying, original)) {
                    results.add(trying);
                }
            }

            // Need to filter attrs!!!
            for (Entry<String, String> eachGoodAttr : Attributes.filterQuality(attrs).entrySet()) {
                // By trying = By.xpath(".//*[@" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                By trying = By.cssSelector(tagName + "[" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                // System.out.println("+++ Trying... " + trying);
                if (isUnique(trying, original)) {
                    results.add(trying);
                }
            }

            // Try XPath text() matches
            if (results.isEmpty() && hasString( original.getText() )) { // Try text
                if (CAN_USE_TEXT_TAGS.apply( tagName )) {  // Anything else?!?
                    By trying = By.xpath("//" + tagName + "[text()='" + original.getText() + "']");
                    if (isUnique(trying, original)) {
                        results.add(trying);
                    }
                }
            }

            // Now it gets more complex - what can we do to normalise...? Either parse and fix original selector or pick a much simpler, purer one?
            // So far, no Id, class, or attr has *uniquely* referenced us...
            // But we surely HAVE to check for some intrinsic attrs, otherwise we very likely will have to use nth-child or bail out.

            if (results.isEmpty()) { // ???
                final String selector = originalSelector.toString();
                final List<String> clauses = Splitter.onPattern("[> /]").trimResults().omitEmptyStrings().splitToList( selector.substring( selector.indexOf(':') + 1) );
                final String mostSpecific = clauses.get( clauses.size() - 1);
//                System.out.println("==> clauses = " + clauses);

                WebElement parent = original;
                while ( /* parent != null && */ (parent = parent.findElement(By.xpath(".."))) != null) {
                    if (parent.getTagName().equals("html")) {
                        break;
                    }

                    Map<String, String> pattrs = attributes(getWrappedDriver(), parent);
                    System.out.println("==> parent: " + parent.getTagName() + " = " + pattrs);

                    final String parentId = pattrs.get("id");
                    if (hasString(parentId) && !Ids.isGeneratedString(parentId)) {  // Need to check other intrinsic props of the parent!
//                        By newGuess = By.cssSelector("#" + pattrs.get("id") + " " + mostSpecific);

                        // FIXME Clarify we have *pivot* here!

                        // See if "#parentId <tag>" is enough...
                        By tryingTag = By.cssSelector(idPrefix(parentId) + " " + tagName);
//                        System.out.println("====> tryingTag = " + tryingTag);

                        if (isUnique(tryingTag, original)) {
                            results.add(tryingTag);
                            parent = null; // FIXME ugh, vile way to break out of outer loop
                            break;
                        }

                        // FIXME Ugh, duplication
                        for (Entry<String, String> eachGoodAttr : Attributes.filterQuality(attrs).entrySet()) {
                            if (tagName.equals("input") && eachGoodAttr.getKey().equals("value") && isNonSemantic( eachGoodAttr.getValue() )) {
                                continue;  // non-semantic
                            }

                            // By trying = By.xpath(".//*[@" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                            By trying = By.cssSelector(idPrefix(parentId) + " " + tagName + "[" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                            System.out.println("+++ Trying parent-based... " + trying);
                            if (isUnique(trying, original)) {
                                results.add(trying);
                                parent = null; // FIXME ugh, vile way to break out of outer loop
                                break;
                            }
                            else {
                                // What else...?
                            }
                        }
                    }

                    if (parent == null) {
                        break;
                    }

                    for (Entry<String, String> eachGoodParentAttr : Attributes.filterQuality(pattrs).entrySet()) {
                        // FIXME Clarify we have *POSSIBLE pivot* here!

                        // By trying = By.xpath(".//*[@" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                        By trying = By.cssSelector(parent.getTagName() + "[" + eachGoodParentAttr.getKey() + "='" + eachGoodParentAttr.getValue() + "']");
                        System.out.println("+++ Trying parent-based Attr... " + trying);
                        // System.out.println("+++ Trying... " + trying);
                        if (isUnique(trying, original)) {
                            results.add(trying);
                            parent = null; // FIXME ugh, vile way to break out of outer loop
                            break;
                        }
                    }

                    if (parent == null) {
                        break;
                    }

                    By got = findWayFromLeafToPossiblePivot(original, mostSpecific, parent);
                    if (got != null && isUnique(got, original)) {
                        results.add(got);
                        parent = null; // FIXME ugh, vile way to break out of outer loop
                        break;
                    }
                }
            }
        }

        return results;
    }

    private By findWayFromLeafToPossiblePivot(WebElement leaf, String originalsOwnClause, WebElement pivot) {
        System.out.println("====== FROM " + leaf + " to " + pivot);

        List<String> sels = new ArrayList<>();

        WebElement parent = leaf;
        while ( /* parent != null && */ (parent = parent.findElement(By.xpath(".."))) != null) {
            // System.out.println("Try: " + parent);
            if (parent.getTagName().equals("html") || parent.equals(pivot)) {
                break;
            }

            List<WebElement> sibs = parent.findElements(By.xpath("preceding-sibling::*"));
            if (sibs.isEmpty()) {
                continue;
            }
            sels.add("div:nth-child(" + (sibs.size() + 1) + ")");
        }

        // System.out.println("======> sels " + sels);

        if (sels.isEmpty()) {  // FIXME Genericise this method!!!!!
            return null;
        }

        ////////////////////////////////////

        Map<String, String> pattrs = attributes(getWrappedDriver(), pivot);
//        System.out.println("==> parent: " + parent.getTagName() + " = " + pattrs);

        final String parentId = pattrs.get("id");
        if (hasString(parentId) && !Ids.isGeneratedString(parentId)) {  // Need to check other intrinsic props of the parent!
            sels.add( idPrefix(parentId) );
        }
        else {
            final Map<String,String> goodAttrs = Attributes.filterQuality(pattrs);

            if (!goodAttrs.isEmpty()) {
                for (Entry<String, String> eachGoodParentAttr : goodAttrs.entrySet()) {
                    sels.add(parent.getTagName() + "[" + eachGoodParentAttr.getKey() + "='" + eachGoodParentAttr.getValue() + "']");
                    break;  // Ugh, really only allow *one* ?!?!?
                }
            }
            else {
                System.out.println("Got nth-child, but no good pivot - skip");
                return null;  // Ugh, not a good pivot!
            }
        }

        // FIXME We probably *should* use > rather than ' ' if we *can*
        By trying = By.cssSelector( Joiner.on(' ').join( Lists.reverse(sels) ) + " " + originalsOwnClause);
        System.out.println("======> trying " + trying);

        return trying;
    }

    private String idPrefix(final String rawId) {
        return "#" + rawId.replaceAll(":", "\\\\3A ");
    }

    private boolean isNonSemantic( String value) {
        if (value.isEmpty()) {
            return false;
        }

        try {
            Double.valueOf(value);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isUnique(final By selector, final WebElement original) {
        List<WebElement> tried = getWrappedDriver().findElements(selector);
        return (tried.size() == 1 && original.equals(tried.get(0)));
    }

    @Override
    public WebElement findElement( final By by) {
        final WebElement original = getWrappedDriver().findElement(by);

        final List<By> newBys = getImprovedSelector(original, by);
        if (!newBys.isEmpty()) {
            System.out.println("> Suggestions... " + newBys);
        }

        return original;
    }

    @Override
    public List<WebElement> findElements( final By by) {
        return getWrappedDriver().findElements(by);
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
}
