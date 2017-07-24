package gumtree.spoon.builder;

import com.github.gumtreediff.tree.ITree;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtNamedElement;
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
		for (ModifierKind kind : m.getModifiers()) {
			ITree modifier = builder.createNode("Modifier", kind.toString());
			modifier.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, m);
			modifiers.addChild(modifier);
		}
		builder.addSiblingNode(modifiers);

	}

	@Override
	public <T> void scanCtVariable(CtVariable<T> e) {
		builder.addSiblingNode(builder.createNode("VARIABLE_TYPE", e.getType().getQualifiedName()));
	}
}
