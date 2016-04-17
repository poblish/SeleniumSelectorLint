package org.hiatusuk.selectorLint;

import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.internal.WrapsDriver;

public abstract class WebDriverWrapper implements WebDriver, WrapsDriver, JavascriptExecutor, HasInputDevices, HasTouchScreen {

    private WebDriver original;

    public WebDriverWrapper(WebDriver originalDriver) {
        this.original = originalDriver;
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
