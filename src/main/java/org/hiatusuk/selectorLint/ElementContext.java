package org.hiatusuk.selectorLint;

import java.util.Map;

import org.openqa.selenium.WebElement;

public interface ElementContext {
    boolean isLeaf();
    WebElement element();
    String currentTagName();
    Map<String,String> attributes();

    String getOriginalSelector();

    boolean skippedUselessElement();

    void setHasSomeProps();
    void setAddedGoodNonUniqueNode();
}
