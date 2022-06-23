package gumtree.spoon.diff;

import com.github.gumtreediff.matchers.Matcher;

/**
 * Stores configuration to compute Diff. This is different from {@link com.github.gumtreediff.matchers.GumtreeProperties}
 * as the latter stores the matching configuration. In other words, it stores parameters on which nodes of AST trees are matched.
 */
public class DiffConfiguration {
    private Matcher matcher;

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }
}
