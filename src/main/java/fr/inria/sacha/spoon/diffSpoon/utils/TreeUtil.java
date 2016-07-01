package fr.inria.sacha.spoon.diffSpoon.utils;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
import fr.inria.sacha.spoon.diffSpoon.Context;
import fr.inria.sacha.spoon.diffSpoon.SpoonGumTreeBuilder;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.ModifierKind;

public final class TreeUtil {
	public static ITree getTree(Context context, CtElement element) {
		final SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder(context);
		scanner.scan(element);
		ITree tree = scanner.getRoot();

		tree.refresh();
		TreeUtils.postOrderNumbering(tree);
		TreeUtils.computeHeight(tree);

		return tree;
	}

	public static ITree createNode(String label, CtElement element, Context context) {
		ITree newNode = createNode(label, SpoonUtil.getTypeName(element.getClass().getSimpleName()), context);

		if (element instanceof CtModifiable) {
			addModifiers(newNode, (CtModifiable) element, context);
		}

		// for some node add the declared static type
		if (element instanceof CtParameter || element instanceof CtField || element instanceof CtLocalVariable) {
			addStaticTypeNode(newNode, (CtTypedElement) element, context);
		}

		newNode.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, element);
		return newNode;
	}

	public static ITree createNode(String label, String typeClass, Context context) {
		return context.createTree(context.resolveTypeId(typeClass), label, typeClass);
	}

	public static void addStaticTypeNode(ITree node, CtTypedElement obj, Context context) {
		ITree modifier = createNode("", "StaticType", context);
		modifier.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, obj);
		modifier.setLabel(obj.getType().getQualifiedName());
		node.addChild(modifier);
	}

	public static void addModifiers(ITree node, CtModifiable obj, Context context) {
		ITree modifiers = createNode("", "Modifiers", context);
		for (ModifierKind kind : obj.getModifiers()) {
			ITree modifier = createNode(kind.toString(), "Modifier", context);
			modifier.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, obj);
			modifiers.addChild(modifier);
		}
		node.addChild(modifiers);
	}

	public static String printTree(String tab, ITree t) {
		final StringBuilder b = new StringBuilder();
		b.append(t.getType()).append(":").append(t.getLabel()).append(" \n");
		for (ITree c : t.getChildren()) {
			b.append(tab).append(" ").append(printTree("\t" + tab, c));
		}
		return b.toString();
	}

	private TreeUtil() {
		throw new AssertionError("No instance.");
	}
}
