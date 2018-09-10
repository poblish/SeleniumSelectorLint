package org.hiatusuk.selectorLint.webdriver;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.openqa.selenium.*;

public class LintedWebElement implements Suggestions, WebElement, WrapsElement {

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
    public <X> X getScreenshotAs( OutputType<X> target) throws WebDriverException {
        throw new UnsupportedOperationException();
    }
}
