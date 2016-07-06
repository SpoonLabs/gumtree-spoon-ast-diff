package gumtree.spoon.builder;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.CtScanner;

import java.util.Stack;

class TreeScanner extends CtScanner {
	private final TreeContext treeContext;
	private final Stack<ITree> nodes = new Stack<>();

	TreeScanner(TreeContext treeContext, ITree root) {
		this.treeContext = treeContext;
		nodes.push(root);
	}

	@Override
	public void enter(CtElement element) {
		if (element instanceof CtReference) {
			nodes.push(null);
			return;
		}

		int size = nodes.size();
		new NodeCreator(this).scan(element);
		if (size == nodes.size()) {
			addNodeToTree(createNode(element, ""));
		}
	}

	@Override
	public void exit(CtElement element) {
		nodes.pop();
		super.exit(element);
	}

	void addNodeToTree(ITree node) {
		ITree parent = nodes.peek();
		if (parent != null) {// happens when nodes.push(null)
			parent.addChild(node);
		}
		nodes.push(node);
	}

	ITree createNode(CtElement element, String label) {
		ITree newNode = createNode(getTypeName(element.getClass().getSimpleName()), label);

		if (element instanceof CtModifiable) {
			addModifiers((CtModifiable) element, newNode);
		}

		// for some node add the declared static type
		if (element instanceof CtParameter || element instanceof CtField || element instanceof CtLocalVariable) {
			addStaticTypeNode((CtTypedElement) element, newNode);
		}

		newNode.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, element);
		return newNode;
	}

	private String getTypeName(String simpleName) {
		// Removes the "Ct" at the beginning and the "Impl" at the end.
		return simpleName.substring(2, simpleName.length() - 4);
	}

	private void addStaticTypeNode(CtTypedElement obj, ITree node) {
		ITree modifier = createNode("StaticType", "");
		modifier.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, obj);
		modifier.setLabel(obj.getType().getQualifiedName());
		node.addChild(modifier);
	}

	private void addModifiers(CtModifiable obj, ITree node) {
		ITree modifiers = createNode("Modifiers", "");
		for (ModifierKind kind : obj.getModifiers()) {
			ITree modifier = createNode("Modifier", kind.toString());
			modifier.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, obj);
			modifiers.addChild(modifier);
		}
		node.addChild(modifiers);
	}

	private ITree createNode(String typeClass, String label) {
		return treeContext.createTree(typeClass.hashCode(), label, typeClass);
	}
}
