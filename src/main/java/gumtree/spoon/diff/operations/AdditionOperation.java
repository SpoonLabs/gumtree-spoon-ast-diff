package gumtree.spoon.diff.operations;

import com.github.gumtreediff.actions.model.Addition;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.declaration.CtElement;

abstract class AdditionOperation<T extends Addition> extends Operation<T> {
	private final CtElement parent;
	private final int position;

	AdditionOperation(T action) {
		super(action);
		position = action.getPosition();
		parent = (CtElement) action.getParent().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
	}

	public int getPosition() {
		return position;
	}

	public CtElement getParent() {
		return parent;
	}
}
