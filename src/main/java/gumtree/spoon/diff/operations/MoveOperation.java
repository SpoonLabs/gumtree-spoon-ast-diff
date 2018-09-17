package gumtree.spoon.diff.operations;

import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.ITree;

import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.declaration.CtElement;

public class MoveOperation extends AdditionOperation<Move> {
	public MoveOperation(Move action) {
		super(action);
	}

	@Override
	public CtElement getDstNode() {
		ITree destNode = (ITree) getAction().getNode().getMetadata(SpoonGumTreeBuilder.DESTINATION_NODE);
		if (destNode != null) {
			Object object = destNode.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			if (object instanceof CtElement) {
				return (CtElement) object;
			} else {
				return (CtElement) destNode.getParent().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			}
		}
		return null;
	}
}
