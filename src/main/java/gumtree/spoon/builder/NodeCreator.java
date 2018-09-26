package gumtree.spoon.builder;

import com.github.gumtreediff.tree.ITree;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtInheritanceScanner;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

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

		// ensuring an order (instead of hashset)
		// otherwise some flaky tests in CI
		Set<ModifierKind> modifiers1 = new TreeSet<>(new Comparator<ModifierKind>() {
			@Override
			public int compare(ModifierKind o1, ModifierKind o2) {
				return o1.name().compareTo(o2.name());
			}
		});
		modifiers1.addAll(m.getModifiers());
		
		for (ModifierKind kind : modifiers1) {
			ITree modifier = builder.createNode("Modifier", kind.toString());
			modifier.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, m);
			modifiers.addChild(modifier);
		}
		builder.addSiblingNode(modifiers);

	}

	@Override
	public <T> void scanCtVariable(CtVariable<T> e) {
		CtTypeReference<T> type = e.getType();
		if (type != null) {
			ITree variableType = builder.createNode("VARIABLE_TYPE", type.getQualifiedName());
			variableType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, type);
			builder.addSiblingNode(variableType);
		}
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> e) {
		// add the return type of the method
		CtTypeReference<T> type = e.getType();
		if (type != null) {
			ITree returnType = builder.createNode("RETURN_TYPE", type.getQualifiedName());
			returnType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, type);
			builder.addSiblingNode(returnType);
		}
		super.visitCtMethod(e);
	}
}
