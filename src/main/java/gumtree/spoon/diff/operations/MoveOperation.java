package gumtree.spoon.diff.operations;

import com.github.gumtreediff.actions.model.Move;

import spoon.reflect.declaration.CtElement;

public class MoveOperation extends TreeAdditionOperation<Move> {
	public MoveOperation(Move action) {
		super(action);
	}

	@Override
	public CtElement getDstNode() {
		return (CtElement) getAction().getNode().getMetadata("spoon_object_dest");
	}
}
