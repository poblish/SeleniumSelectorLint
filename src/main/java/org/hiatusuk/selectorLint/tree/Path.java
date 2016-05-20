package org.hiatusuk.selectorLint.tree;

import java.util.Objects;

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

    public Path append(final CharSequence selector, boolean isDirect) {
        if (currentPath.length() > 0) {
            currentPath.append(isDirect ? " > " : " ");
        }
        currentPath.append(selector);
        score++;
        return this;
    }

    public String getPath() {
        return currentPath.toString();
    }

    @Override
    public int compareTo( Path other) {
        int sizeVal = Ints.compare(score, other.score);  // Smallest is best/first
        if (sizeVal != 0) {
            return sizeVal;
        }
        return getPath().compareTo( other.getPath() );
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentPath.toString(), score);
    }

    @Override
    public boolean equals( Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Path)) {
            return false;
        }
        final Path other = (Path) obj;
        return Objects.equals(score, other.score) && Objects.equals(currentPath.toString(), other.currentPath.toString());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("score", score).add("path", currentPath).toString();
    }
}