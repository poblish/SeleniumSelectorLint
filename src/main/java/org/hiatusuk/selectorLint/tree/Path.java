package org.hiatusuk.selectorLint.tree;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Ints;

public class Path implements Comparable<Path> {
    private StringBuilder currentPath;
    private int score;

    public Path() {
        this.currentPath = new StringBuilder();
        this.score = 0;
    }

    public Path(final Path existing) {
        this.currentPath = new StringBuilder( existing.currentPath );
        this.score = existing.score;
    }

    public void append(final CharSequence selector, boolean isDirect) {
        if (currentPath.length() > 0) {
            currentPath.append(isDirect ? " > " : " ");
        }
        currentPath.append(selector);
        score++;
    }

    @Override
    public int compareTo( Path other) {
        int sizeVal = Ints.compare(score, other.score);  // Smallest is best/first
        if (sizeVal != 0) {
            return sizeVal;
        }
        return currentPath.toString().compareTo( other.currentPath.toString() );
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("score", score).add("path", currentPath).toString();
    }
}