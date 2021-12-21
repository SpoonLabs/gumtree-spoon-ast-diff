package gumtree.spoon.diff.support;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.tree.Tree;

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
			Tree matchingNode = isFromSource ? mapping.first : mapping.second;
			CtElement associatedElement = (CtElement) matchingNode.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			if (elementToMatch == associatedElement) {
				Tree linked = isFromSource ? diff.getMappingsComp().getDstForSrc(matchingNode)
						: diff.getMappingsComp().getSrcForDst(matchingNode);
				return (CtElement) linked.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			}
		}

		return null;
	}
}
