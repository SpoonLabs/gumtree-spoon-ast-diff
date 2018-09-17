package gumtree.spoon.diff.operations;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;

import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;

public abstract class Operation<T extends Action> {
	private final CtElement node;
	private final Object value;
	private final CtRole role;
	private final T action;

	public Operation(T action) {
		this.action = action;
		Object object = action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		if (object instanceof CtElement) {
			this.node = (CtElement) object;
			this.value = null;
		} else {
			this.value = object;
			this.node = (CtElement) action.getNode().getParent().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		}
		this.role = CtRole.values()[action.getNode().getType()];
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
		return toStringAction(action);
	}

	private String toStringAction(Action action) {
		String newline = System.getProperty("line.separator");
		StringBuilder stringBuilder = new StringBuilder();


		Object value = action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		CtElement element;
		if (value instanceof CtElement) {
			element = (CtElement) value;
			value = null;
		} else {
			element = (CtElement) action.getNode().getParent().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		}
		// action name
		stringBuilder.append(action.getClass().getSimpleName());

		// node type
		String nodeType = element.getClass().getSimpleName();
		nodeType = nodeType.substring(2, nodeType.length() - 4);
		stringBuilder.append(" ").append(nodeType);

		// action position
		CtElement parent = element;
		while (parent.getParent() != null && !(parent.getParent() instanceof CtPackage)) {
			parent = parent.getParent();
		}
		String position = " at ";
		if (parent instanceof CtType) {
			position += ((CtType) parent).getQualifiedName();
		}
		if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
			position += ":" + element.getPosition().getLine();
		}
		if (action instanceof Move) {
			ITree destNode = (ITree) action.getNode().getMetadata(SpoonGumTreeBuilder.DESTINATION_NODE);
			Object destValue = destNode.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			CtElement elementDest;
			if (value instanceof CtElement) {
				elementDest = (CtElement) destValue;
				destValue = null;
			} else {
				elementDest = (CtElement) destNode.getParent().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			}
			position = " from " + element.getParent(CtClass.class).getQualifiedName();
			if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
				position += ":" + element.getPosition().getLine();
			}
			position += " to " + elementDest.getParent(CtClass.class).getQualifiedName();
			if (elementDest.getPosition() != null && !(elementDest.getPosition() instanceof NoSourcePosition)) {
				position += ":" + elementDest.getPosition().getLine();
			}
		}
		stringBuilder.append(position).append(newline);

		// code change
		String label = partialElementPrint(element);
		if (action instanceof Move) {
			label = element.toString();
		}
		if (action instanceof Update) {
			ITree destNode = (ITree) action.getNode().getMetadata(SpoonGumTreeBuilder.DESTINATION_NODE);
			Object destValue = destNode.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			CtElement elementDest;
			if (value instanceof CtElement) {
				elementDest = (CtElement) destValue;
				destValue = null;
			} else {
				elementDest = (CtElement) destNode.getParent().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			}
			label += " to " + elementDest.toString();
		}
		String[] split = label.split(newline);
		for (String s : split) {
			stringBuilder.append("\t").append(s).append(newline);
		}
		return stringBuilder.toString();
	}

	private String partialElementPrint(CtElement element) {
		DefaultJavaPrettyPrinter print = new DefaultJavaPrettyPrinter(element.getFactory().getEnvironment()) {
			@Override
			public DefaultJavaPrettyPrinter scan(CtElement e) {
				if (e != null && e.getMetadata("isMoved") == null) {
					return super.scan(e);
				}
				return this;
			}
		};

		print.scan(element);
		return print.getResult();
	}

	/** returns the changed (inserted/deleted/updated) element */
	public CtElement getSrcNode() {
		return node;
	}

	/** returns the new version of the node (only for update) */
	public CtElement getDstNode() {
		return null;
	}

	public Object getValue() {
		return value;
	}

	public CtRole getRole() {
		return role;
	}

}
