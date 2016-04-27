package org.hiatusuk.selectorLint;

import org.openqa.selenium.By;

public interface MatchTester {
    boolean ok(final By selector);
}
