package org.hiatusuk.selectorLint;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Simplifier {

    private final WebDriver driver;

    public Simplifier(final WebDriver inDriver) {
        driver = checkNotNull(inDriver);
    }

    public void handleLocalClass(final WebElement original, final String tagName, final Map<String, String> attrs, final List<By> ioResults) {
        // Need to filter, or at least *score* these!
        for (String eachClass : Classes.filter( attrs.get("class") )) {  // Will never be empty strings
            By trying = By.cssSelector(tagName + "." + CssUtils.cssEscape(eachClass));  // Best of all: more testable than By.className, handles multiple classnames 
            if (isUnique(trying, original)) {
                ioResults.add(trying);
            }
        }
    }

    public void handleLocalAttrs(final WebElement original, final String tagName, final Map<String, String> attrs, final List<By> ioResults) {
        // Need to filter attrs!!!
        for (Entry<String, String> eachGoodAttr : Attributes.filterQuality(attrs).entrySet()) {
            if (tagName.equals("input") && eachGoodAttr.getKey().equals("value") && isNonSemantic( eachGoodAttr.getValue() )) {
                continue;  // non-semantic
            }

            // By trying = By.xpath(".//*[@" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
            By trying = By.cssSelector(tagName + "[" + eachGoodAttr.getKey() + "='" + eachGoodAttr.getValue() + "']");
            // System.out.println("+++ Trying... " + trying);
            if (isUnique(trying, original)) {
                ioResults.add(trying);
            }
        }
    }

    public boolean isNonSemantic( String value) {
        return (value.equals("0") || value.equals("1") || Ids.PATT.matcher(value).find());
    }

    public boolean isUnique(final By selector, final WebElement original) {
        List<WebElement> tried = driver.findElements(selector);
        return (tried.size() == 1 && original.equals(tried.get(0)));
    }
}
