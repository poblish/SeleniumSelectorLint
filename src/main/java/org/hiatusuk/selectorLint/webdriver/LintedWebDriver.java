package org.hiatusuk.selectorLint.webdriver;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hiatusuk.selectorLint.impl.Simplifier;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.slf4j.Logger;

public class LintedWebDriver implements WebDriver, WrapsDriver, JavascriptExecutor, HasInputDevices, HasTouchScreen {

    private final WebDriver original;
    private Logger suggestionsLogger = null;

    private final Simplifier simplifier;

    public LintedWebDriver(WebDriver originalDriver, final Simplifier simplifier) {
        this.original = checkNotNull(originalDriver);
        this.simplifier = checkNotNull(simplifier);
    }

    public void logSuggestions(final Logger logger) {
        this.suggestionsLogger = logger;
    }

    @Override
    public WebElement findElement( final By by) {
//        long startNs = System.nanoTime();
        final WebElement original = getWrappedDriver().findElement(by);
//        double diffMs = (System.nanoTime() - startNs) / 1E6;

        final List<By> newBys = simplifier.getImprovedSelector(Collections.singletonList(original), by.toString());

        if (suggestionsLogger != null) {
            if (newBys.isEmpty()) {
                suggestionsLogger.debug("> No Suggestions for [" + by + "]");
            }
            else {
                suggestionsLogger.debug("> Suggestions for [" + by + "] ... " + newBys);
            }
        }

        return new LintedWebElement(original, newBys);
    }

    @Override
    public List<WebElement> findElements( final By by) {
//        long startNs = System.nanoTime();
        final List<WebElement> originals = getWrappedDriver().findElements(by);
//        double diffMs = (System.nanoTime() - startNs) / 1E6;

        final List<By> newBys = simplifier.getImprovedSelector(originals, by.toString());

        if (suggestionsLogger != null) {
            if (newBys.isEmpty()) {
                suggestionsLogger.debug("> NO Suggestions for [" + by + "]");
            }
            else {
                suggestionsLogger.debug("> Suggestions for [" + by + "] ... " + newBys);
            }
        }

        return new LintedWebElements(originals, newBys);
    }

    @Override
    public final WebDriver getWrappedDriver() {
        return original;
    }

    @Override
    public void get( String url) {
        simplifier.clearCache();
        getWrappedDriver().get(url);
    }

    @Override
    public String getCurrentUrl() {
        return getWrappedDriver().getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return getWrappedDriver().getTitle();
    }

    @Override
    public String getPageSource() {
        return getWrappedDriver().getPageSource();
    }

    @Override
    public void close() {
        getWrappedDriver().close();
    }

    @Override
    public void quit() {
        getWrappedDriver().quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return getWrappedDriver().getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return getWrappedDriver().getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return getWrappedDriver().switchTo();
    }

    @Override
    public Navigation navigate() {
        return getWrappedDriver().navigate();
    }

    @Override
    public Options manage() {
        return getWrappedDriver().manage();
    }

    @Override
    public TouchScreen getTouch() {
        return ((HasTouchScreen) getWrappedDriver()).getTouch();
    }

    @Override
    public Keyboard getKeyboard() {
        return ((HasInputDevices) getWrappedDriver()).getKeyboard();
    }

    @Override
    public Mouse getMouse() {
        return ((HasInputDevices) getWrappedDriver()).getMouse();
    }

    @Override
    public Object executeScript( String script, Object... args) {
        return ((JavascriptExecutor) getWrappedDriver()).executeScript(script, args);
    }

    @Override
    public Object executeAsyncScript( String script, Object... args) {
        return ((JavascriptExecutor) getWrappedDriver()).executeAsyncScript(script, args);
    }
}
