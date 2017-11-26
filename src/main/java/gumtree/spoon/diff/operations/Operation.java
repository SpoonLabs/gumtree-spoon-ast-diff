package gumtree.spoon.diff.operations;

import com.github.gumtreediff.actions.model.Action;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.declaration.CtElement;

public abstract class Operation<T extends Action> {
	private final CtElement node;
	private final T action;

	public Operation(T action) {
		this.action = action;
		this.node = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
	}

	/** use {@link #getSrcNode()} or {@link #getDstNode()} instead. */
	@Deprecated
	public CtElement getNode() {
		return node;
	}

	public T getAction() {
		return action;
	}

	@Override
	public String toString() {
		return action.toString();
	}

	/** returns the changed (inserded/deleted/updated) element */
	public CtElement getSrcNode() {
		return node;
	}

	/** returns the new version of the node (only for update) */
	public CtElement getDstNode() {
		return null;
	}

}
