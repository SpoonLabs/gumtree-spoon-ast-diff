package gumtree.spoon.diff;

import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.Matcher;

/**
 * Stores configuration to compute Diff
 */
public class DiffConfiguration {
	
	private Matcher matcher;

	private GumtreeProperties gumtreeProperties;

	public GumtreeProperties getGumtreeProperties() {
		return gumtreeProperties;
	}

	public void setGumtreeProperties(GumtreeProperties gumtreeProperties) {
		this.gumtreeProperties = gumtreeProperties;
	}

	public Matcher getMatcher() {
		return matcher;
	}

	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}
}
