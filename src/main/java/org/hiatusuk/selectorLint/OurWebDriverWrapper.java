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

    private final Simplifier simplifier;

    public OurWebDriverWrapper(WebDriver originalDriver) {
        super(originalDriver);
        simplifier = new Simplifier(originalDriver);
    }

    public List<By> getImprovedSelector( final WebElement original, final String originalSelectorString) {
        final List<By> results = new ArrayList<>();
        final String tagName = original.getTagName();

        final Map<String, String> attrs = attributes(getWrappedDriver(), original);

        if (Tags.isGoodQuality(original)) {
            By trying = By.tagName(tagName);
            if (simplifier.isUnique(trying, original)) {

                if (originalSelectorString.equalsIgnoreCase("By.cssSelector: " + tagName) ||
                    originalSelectorString.equalsIgnoreCase("By.tagName: " + tagName)) {
                    // If CSS 'body' was passed in, don't propose pointlessly changing to tagName syntax
                    return results;
                }

                results.add(trying);
                return results;
            }
        }

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
            }
            else {
                By trying = By.id(id);
                if (simplifier.isUnique(trying, original)) {
                    results.add(trying);
                }
            }
            return results;
        }

        simplifier.handleLocalClass(original, tagName, attrs, results);
        simplifier.handleLocalAttrs(original, tagName, attrs, results);

        // Try XPath text() matches
        if (results.isEmpty() && hasString( original.getText() )) { // Try text
            if (CAN_USE_TEXT_TAGS.apply( tagName )) {  // Anything else?!?
                By trying = By.xpath("//" + tagName + "[text()='" + original.getText() + "']");
                if (simplifier.isUnique(trying, original)) {
                    results.add(trying);
                }
            }
        }

        if (!results.isEmpty()) {  // Is this premature?!?
            return results;
        }

        // Now it gets more complex - what can we do to normalise...? Either parse and fix original selector or pick a much simpler, purer one?
        // So far, no Id, class, or attr has *uniquely* referenced us...
        // But we surely HAVE to check for some intrinsic attrs, otherwise we very likely will have to use nth-child or bail out.

        final List<String> clauses = Splitter.onPattern("[> /]").trimResults().omitEmptyStrings().splitToList( originalSelectorString.substring( originalSelectorString.indexOf(':') + 1) );
        final String mostSpecific = clauses.get( clauses.size() - 1);
//                System.out.println("==> clauses = " + clauses);

        WebElement parent = original;
        while ((parent = parent.findElement(By.xpath(".."))) != null) {
            System.out.println(":: " + parent);

            if (parent.getTagName().equals("html")) {
                break;
            }

            Map<String, String> pattrs = attributes(getWrappedDriver(), parent);
            // System.out.println("==> parent: " + parent.getTagName() + " = " + pattrs);

            final String parentId = pattrs.get("id");
            if (hasString(parentId) && !Ids.isGeneratedString(parentId)) {  // Need to check other intrinsic props of the parent!
//              By newGuess = By.cssSelector("#" + pattrs.get("id") + " " + mostSpecific);

                // FIXME Clarify we have *pivot* here!

                // See if "#parentId <tag>" is enough...
                By tryingTag = By.cssSelector(idPrefix(parentId) + " " + tagName);
//                        System.out.println("====> tryingTag = " + tryingTag);

                if (simplifier.isUnique(tryingTag, original)) {
                    results.add(tryingTag);
                    parent = null; // FIXME ugh, vile way to break out of outer loop
                    break;
                }

                // FIXME Ugh, duplication
                for (Entry<String, String> eachGoodAttr : Attributes.filterQuality(attrs).entrySet()) {
                    if (tagName.equals("input") && eachGoodAttr.getKey().equals("value") && simplifier.isNonSemantic( eachGoodAttr.getValue() )) {
                        continue;  // non-semantic
                    }

                    // By trying = By.xpath(".//*[@" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                    By trying = By.cssSelector(idPrefix(parentId) + " " + tagName + "[" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                    System.out.println("+++ Trying parent-based... " + trying);
                    if (simplifier.isUnique(trying, original)) {
                        results.add(trying);
                        return results;
                    }
                    else {
                        // What else...?
                    }
                }
            }

            for (Entry<String, String> eachGoodParentAttr : Attributes.filterQuality(pattrs).entrySet()) {
                // FIXME Clarify we have *POSSIBLE pivot* here!

                // By trying = By.xpath(".//*[@" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
                By trying = By.cssSelector(parent.getTagName() + "[" + eachGoodParentAttr.getKey() + "='" + eachGoodParentAttr.getValue() + "']");
                System.out.println("+++ Trying parent-based Attr... " + trying);
                // System.out.println("+++ Trying... " + trying);
                if (simplifier.isUnique(trying, original)) {
                    results.add(trying);
                    return results;
                }
            }

            By got = findWayFromLeafToPossiblePivot(original, mostSpecific, parent);
            if (got != null && simplifier.isUnique(got, original)) {
                results.add(got);
                return results;
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

//        if (Tags.isGoodQuality(pivot)) {
//            By trying = By.tagName(tagName);
//            if (simplifier.isUnique(trying, original)) {
//                
//            }
//        }

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
        System.out.println("=> Try pivot query... " + trying);

        return trying;
    }

    private String idPrefix(final String rawId) {
        return "#" + rawId.replaceAll(":", "\\\\3A ");
    }

    @Override
    public WebElement findElement( final By by) {
        final WebElement original = getWrappedDriver().findElement(by);

        final List<By> newBys = getImprovedSelector(original, by.toString());
        if (!newBys.isEmpty()) {
            System.out.println("> Suggestions... " + newBys);
        }

        return original;
    }

    @Override
    public List<WebElement> findElements( final By by) {
        final List<WebElement> originals = getWrappedDriver().findElements(by);

        for (WebElement each : originals) {
            final List<By> newBys = getImprovedSelector(each, by.toString());
            if (!newBys.isEmpty()) {
                System.out.println("> Suggestions... " + newBys);
            }
        }

        return originals;
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
