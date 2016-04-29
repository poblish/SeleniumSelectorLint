package org.hiatusuk.selectorLint;

import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OurWebDriverWrapper extends WebDriverWrapper {

    private final Simplifier simplifier;

    public OurWebDriverWrapper(WebDriver originalDriver) {
        super(originalDriver);
        simplifier = new Simplifier(originalDriver);
    }

    public List<By> getImprovedSelector( final List<WebElement> originalMatches, final String originalSelectorString) {
        return simplifier.getImprovedSelector( originalMatches, originalSelectorString);
    }

    @Override
    public WebElement findElement( final By by) {
        final WebElement original = getWrappedDriver().findElement(by);

        final List<By> newBys = getImprovedSelector(Collections.singletonList(original), by.toString());
        if (newBys.isEmpty()) {
            System.out.println("> NO Suggestions for [" + by + "]");
        }
        else {
            System.out.println("> Suggestions for [" + by + "]... " + newBys);
        }

        return original;
    }

    @Override
    public List<WebElement> findElements( final By by) {
        final List<WebElement> originals = getWrappedDriver().findElements(by);

        final List<By> newBys = getImprovedSelector(originals, by.toString());
        if (newBys.isEmpty()) {
            System.out.println("> NO Suggestions for [" + by + "]");
        }
        else {
            System.out.println("> Suggestions for [" + by + "]... " + newBys);
        }

        return originals;
    }
}
