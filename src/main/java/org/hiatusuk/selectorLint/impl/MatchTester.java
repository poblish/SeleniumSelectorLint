package org.hiatusuk.selectorLint.impl;

import org.openqa.selenium.By;

public interface MatchTester {
    boolean ok(final By selector);
}
