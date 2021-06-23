package gumtree.spoon.builder;

import java.util.Collection;
import java.util.Collections;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;

/**
 * Represents a virtual node that groups other CtElements in a tree format,
 * being this object the root of that three. It's an special case of Wrap: here
 * we wrap a string that describes the virtual node
 * 
 */
public class CtVirtualElement extends CtWrapper<String> {

	protected Collection<?> children;

	public CtVirtualElement(String wrapped, CtElement parent, Collection<?> children) {
		super(wrapped, parent);
		this.children = children;
	}

	public CtVirtualElement(String wrapped, CtElement parent, Collection<?> children, CtRole roleInParent) {
		super(wrapped, parent, roleInParent);
		this.children = children;
	}

	public Collection<?> getChildren() {
		return Collections.unmodifiableCollection(children);
	}

	@Override
	public String toString() {
		return "VE: " + ((value != null) ? (value.toString()) : null);
	}

	@Override
	public boolean equals(Object o) {

		if (!(o instanceof CtVirtualElement)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		CtVirtualElement anotherVirtual = (CtVirtualElement) o;
		if (this.value == null && anotherVirtual.value == null)
			return true;

		return (this.value != null) && this.value.equals(anotherVirtual.value);
	}

}
