package org.hiatusuk.selectorLint;

import org.hiatusuk.selectorLint.config.Options;
import org.hiatusuk.selectorLint.impl.Simplifier;
import org.hiatusuk.selectorLint.webdriver.LintedWebDriver;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

public class Linter {

    private final LintedWebDriver wrapper;
    private final Simplifier simplifier;

    private Linter(final WebDriver driver) {
        simplifier = new Simplifier(driver);
        wrapper = new LintedWebDriver(driver, simplifier);
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

    public LintedWebDriver build() {
        return wrapper;
    }
}
