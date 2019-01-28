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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + position;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdditionOperation other = (AdditionOperation) obj;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (position != other.position)
			return false;
		return true;
	}
}
