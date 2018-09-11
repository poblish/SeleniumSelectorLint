package org.hiatusuk.selectorLint.webdriver;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class LintedWebElements extends ArrayList<WebElement> implements Suggestions {

    private final List<By> suggestedSelectors;

    private static final long serialVersionUID = 1L;

    public LintedWebElements(final List<WebElement> originals, final List<By> suggestedSelectors) {
        addAll(originals);
        this.suggestedSelectors = checkNotNull(suggestedSelectors);
    }

    @Override
    public List<By> getSuggestedSelectors() {
        return suggestedSelectors;
    }
}