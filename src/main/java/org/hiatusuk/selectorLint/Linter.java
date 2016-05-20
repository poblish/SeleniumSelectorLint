package org.hiatusuk.selectorLint;

import org.openqa.selenium.WebDriver;

public class Linter {

    private final WebDriverWrapper wrapper;
    private final Simplifier simplifier;

    private Linter(final WebDriver driver) {
        simplifier = new Simplifier(driver);
        wrapper = new WebDriverWrapper(driver, simplifier);
    }

    public static Linter wrap(final WebDriver driver) {
        return new Linter(driver);
    }

    public Linter config(final Options opts) {
        simplifier.setOptions(opts);
        return this;
    }

    public Linter logSuggestions(final Logger logger) {
        wrapper.logSuggestions(logger);
        return this;
    }

    public Linter convertCssToXPath(final boolean convertCss) {
        simplifier.convertCssToXPath(convertCss);
        return this;
    }

    public WebDriverWrapper build() {
        return wrapper;
    }
}
