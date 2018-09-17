package gumtree.spoon.diff.operations;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;

import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;

public class UpdateOperation extends Operation<Update> {
	private final CtElement destElement;
	private final Object destValue;
	private final CtRole destRole;

	public UpdateOperation(Update action) {
		super(action);
		ITree destNode = (ITree) action.getNode().getMetadata(SpoonGumTreeBuilder.DESTINATION_NODE);
		if (destNode != null) {
			Object object = destNode.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			if (object instanceof CtElement) {
				this.destElement = (CtElement) object;
				this.destValue = null;
			} else {
				this.destValue = object;
				this.destElement = (CtElement) destNode.getParent().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			}
			this.destRole = CtRole.values()[destNode.getType()];
		} else {
			destElement = null;
			destValue = null;
			destRole = null;
		}
	}

	@Override
	public CtElement getDstNode() {
		return destElement;
	}

}
