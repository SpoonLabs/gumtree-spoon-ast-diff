package gumtree.spoon.builder;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtPath;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.CtVisitor;
import spoon.support.reflect.declaration.CtElementImpl;

/**
 * This class wraps objects that do not inherit from CtElement.
 * 
 * Represents Information that does not exist in the Spoon hierarchy i.e., it
 * does not inherit from CtElement, but exists somewhere in the model (e.g., an
 * attribute of a particular CtElement)
 * 
 */
public class CtWrapper<L> extends CtElementImpl {

	/**
	 * The object to be wrapped
	 */
	protected L value;

	protected CtRole roleInParent;

	public CtWrapper(L wrapped, CtElement parent) {
		super();
		this.value = wrapped;
		this.parent = parent;
		setFactory(parent.getFactory());
	}

	public CtWrapper(L wrapped, CtElement parent, CtRole roleInParent) {
		super();
		this.value = wrapped;
		this.parent = parent;
		this.roleInParent = roleInParent;
		setFactory(parent.getFactory());
	}

	@Override
	public void accept(CtVisitor visitor) {

	}

	@Override
	public String toString() {
		return (value != null) ? value.toString() : null;
	}

	public L getValue() {
		return value;
	}

	public void setValue(L wrapped) {
		this.value = wrapped;
	}

	@Override
	public boolean equals(Object o) {

		if (!(o instanceof CtWrapper)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		CtWrapper anotherWrap = (CtWrapper) o;
		if (this.value == null && anotherWrap.value == null)
			return true;

		return (this.value != null) && this.value.equals(anotherWrap.value);
	}

	@Override
	public CtRole getRoleInParent() {
		if (this.roleInParent != null)
			return this.roleInParent;
		return parent.getRoleInParent();
	}

	@Override
	public CtPath getPath() {
		return parent.getPath();
	}
}
