package org.hiatusuk.selectorLint.webdriver;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.internal.*;

public class LintedWebElement implements Suggestions, FindsById, FindsByLinkText, FindsByXPath, FindsByTagName, FindsByCssSelector, WebElement, WrapsElement {

    private final WebElement target;
    private final List<By> suggestedSelectors;

    public LintedWebElement(final WebElement target, final List<By> suggestedSelectors) {
        this.target = checkNotNull(target);
        this.suggestedSelectors = checkNotNull(suggestedSelectors);
    }

    public List<By> getSuggestedSelectors() {
        return suggestedSelectors;
    }

    @Override
    public WebElement getWrappedElement() {
        return target;
    }

    @Override
    public void click() {
        target.click();
    }

    @Override
    public void submit() {
        target.submit();
    }

    @Override
    public void sendKeys( CharSequence... keysToSend) {
        target.sendKeys(keysToSend);
    }

    @Override
    public void clear() {
        target.clear();
    }

    @Override
    public String getTagName() {
        return target.getTagName();
    }

    @Override
    public String getAttribute( String name) {
        return target.getAttribute(name);
    }

    @Override
    public boolean isSelected() {
        return target.isSelected();
    }

    @Override
    public boolean isEnabled() {
        return target.isEnabled();
    }

    @Override
    public String getText() {
        return target.getText();
    }

    @Override
    public List<WebElement> findElements( By by) {
        return target.findElements(by);
    }

    @Override
    public WebElement findElement( By by) {
        return target.findElement(by);
    }

    @Override
    public boolean isDisplayed() {
        return target.isDisplayed();
    }

    @Override
    public Point getLocation() {
        return target.getLocation();
    }

    @Override
    public Dimension getSize() {
        return target.getSize();
    }

    @Override
    public Rectangle getRect() {
        return target.getRect();
    }

    @Override
    public String getCssValue( String propertyName) {
        return target.getCssValue(propertyName);
    }

    @Override
    public WebElement findElementByCssSelector( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<WebElement> findElementsByCssSelector( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebElement findElementByTagName( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<WebElement> findElementsByTagName( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebElement findElementByXPath( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<WebElement> findElementsByXPath( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebElement findElementByLinkText( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<WebElement> findElementsByLinkText( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebElement findElementByPartialLinkText( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<WebElement> findElementsByPartialLinkText( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebElement findElementById( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<WebElement> findElementsById( String using) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <X> X getScreenshotAs( OutputType<X> target) throws WebDriverException {
        throw new UnsupportedOperationException();
    }
}
