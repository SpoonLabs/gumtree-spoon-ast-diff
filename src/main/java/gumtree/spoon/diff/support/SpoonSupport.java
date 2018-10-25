package gumtree.spoon.diff.support;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.tree.ITree;

import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.Diff;
import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public class SpoonSupport {

	public CtElement getMappedElement(Diff diff, CtElement elementToMatch, boolean isFromSource) {

		for (Mapping mapping : diff.getMappingsComp().asSet()) {
			ITree matchingNode = isFromSource ? mapping.getFirst() : mapping.getSecond();
			CtElement associatedElement = (CtElement) matchingNode.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			if (elementToMatch == associatedElement) {
				ITree linked = isFromSource ? diff.getMappingsComp().getDst(matchingNode)
						: diff.getMappingsComp().getSrc(matchingNode);
				return (CtElement) linked.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			}
		}

		return null;
	}
}
