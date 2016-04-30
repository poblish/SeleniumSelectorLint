package org.hiatusuk.selectorLint;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.internal.WrapsDriver;

public class WebDriverWrapper implements WebDriver, WrapsDriver, JavascriptExecutor, HasInputDevices, HasTouchScreen {

    private WebDriver original;

    private final Simplifier simplifier;

    public WebDriverWrapper(WebDriver originalDriver) {
        this.original = originalDriver;
        simplifier = new Simplifier(originalDriver);
    }

    public List<By> getImprovedSelector( final List<WebElement> originalMatches, final String originalSelectorString) {
        return simplifier.getImprovedSelector( originalMatches, originalSelectorString);
    }

    @Override
    public WebElement findElement( final By by) {
        long startNs = System.nanoTime();
        final WebElement original = getWrappedDriver().findElement(by);
        double diffMs = (System.nanoTime() - startNs) / 1E6;

        final List<By> newBys = getImprovedSelector(Collections.singletonList(original), by.toString());
        if (newBys.isEmpty()) {
            System.out.println("> NO Suggestions for [" + by + "]");
        }
        else {
            System.out.println("> Suggestions for [" + by + "]" + /* " (" + (int)( diffMs * 1000)/1000.0 + " msecs)" + */ "... " + newBys);
        }

        return original;
    }

    @Override
    public List<WebElement> findElements( final By by) {
        long startNs = System.nanoTime();
        final List<WebElement> originals = getWrappedDriver().findElements(by);
        double diffMs = (System.nanoTime() - startNs) / 1E6;

        final List<By> newBys = getImprovedSelector(originals, by.toString());
        if (newBys.isEmpty()) {
            System.out.println("> NO Suggestions for [" + by + "]");
        }
        else {
            System.out.println("> Suggestions for [" + by + "]" + /* " (" + (int)( diffMs * 1000)/1000.0 + " msecs)" + */ "... " + newBys);
        }

        return originals;
    }

    @Override
    public final WebDriver getWrappedDriver() {
        return original;
    }

    @Override
    public void get( String url) {
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
