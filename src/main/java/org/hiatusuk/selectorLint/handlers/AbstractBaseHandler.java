package org.hiatusuk.selectorLint.handlers;

public abstract class AbstractBaseHandler implements ElementHandler {

    @Override
    public boolean shouldSkip( String tagName) {
        return false;
    }
}