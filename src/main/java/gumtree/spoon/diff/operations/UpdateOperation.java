package gumtree.spoon.diff.operations;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Update;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.declaration.CtElement;

public class UpdateOperation extends Operation<Update> {
	private final CtElement destElement;

	public UpdateOperation(Update action) {
		super(action);
		destElement = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
	}

	@Override
	public CtElement getDstNode() {
		return destElement;
	}

}
