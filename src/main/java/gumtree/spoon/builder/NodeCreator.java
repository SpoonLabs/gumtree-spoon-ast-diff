package gumtree.spoon.builder;

import com.github.gumtreediff.tree.ITree;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.CtInheritanceScanner;

/** responsible to add additional nodes
 * only overrides scan* to add new nodes */
class NodeCreator extends CtInheritanceScanner {
	private final TreeScanner builder;

	NodeCreator(TreeScanner builder) {
		this.builder = builder;
	}

	@Override
	public void scanCtModifiable(CtModifiable m) {
		ITree modifiers = builder.createNode("Modifiers", "");
		modifiers.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, m);
		for (ModifierKind kind : m.getModifiers()) {
			ITree modifier = builder.createNode("Modifier", kind.toString());
			modifier.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, m);
			modifiers.addChild(modifier);
		}
		builder.addSiblingNode(modifiers);

	}

	@Override
	public <T> void scanCtVariable(CtVariable<T> e) {
		ITree variableType = builder.createNode("VARIABLE_TYPE", e.getType().getQualifiedName());
		variableType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, e.getType());
		builder.addSiblingNode(variableType);
	}
}
