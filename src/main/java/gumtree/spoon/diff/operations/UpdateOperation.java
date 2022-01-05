package gumtree.spoon.diff.operations;

import com.github.gumtreediff.actions.model.Update;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;

public class UpdateOperation extends Operation<Update> {
	private final CtElement destElement;
	private final CtRole role;

	public UpdateOperation(Update action) {
		super(action);
		destElement = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
		role = (CtRole) action.getNode().getMetadata(SpoonGumTreeBuilder.ROLE_OF_LABEL_IN_ELEMENT);
	}

	@Override
	public CtElement getDstNode() {
		return destElement;
	}

	public CtRole getUpdatedRole() {
		return role;
	}

}
