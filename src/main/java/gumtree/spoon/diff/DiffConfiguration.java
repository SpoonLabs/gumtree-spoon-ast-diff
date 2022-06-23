package gumtree.spoon.diff;

import com.github.gumtreediff.matchers.Matcher;

/**
 * Stores configuration to compute Diff. This is different from {@link com.github.gumtreediff.matchers.GumtreeProperties}.
 * `DiffConfiguration` stores properties which are more general in nature and are not specific to matchers.
 *
 * In the future, this class can also be wrapper to store all types of configuration, whether required by gumtree or gumtree-spoon.
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
