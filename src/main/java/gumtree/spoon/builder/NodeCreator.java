package gumtree.spoon.builder;

import com.github.gumtreediff.tree.ITree;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtInheritanceScanner;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * responsible to add additional nodes only overrides scan* to add new nodes
 */
public class NodeCreator extends CtInheritanceScanner {
	public static final String MODIFIERS = "Modifiers_";
	private final TreeScanner builder;

	NodeCreator(TreeScanner builder) {
		this.builder = builder;
	}

	@Override
	public void scanCtModifiable(CtModifiable m) {

		if (m.getModifiers().isEmpty())
			return;

		// We add the type of modifiable element
		String type = MODIFIERS + getClassName(m.getClass().getSimpleName());
		ITree modifiers = builder.createNode(type, "");

		// We create a virtual node
		modifiers.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, new CtVirtualElement(type, m, m.getModifiers(), CtRole.MODIFIER));

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
			modifiers.addChild(modifier);
			// We wrap the modifier (which is not a ctelement)
			modifier.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, new CtWrapper(kind, m, CtRole.MODIFIER));
		}
		builder.addSiblingNode(modifiers);

	}

	private String getClassName(String simpleName) {
		if (simpleName == null)
			return "";
		return simpleName.replace("Ct", "").replace("Impl", "");
	}

	@Override
	public <T> void scanCtVariable(CtVariable<T> e) {
		CtTypeReference<T> type = e.getType();
		if (type != null) {
			ITree variableType = builder.createNode("VARIABLE_TYPE", type.getQualifiedName());
			variableType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, type);
			type.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, variableType);
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
			type.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, returnType);
			builder.addSiblingNode(returnType);
		}

		for (CtTypeReference thrown : e.getThrownTypes()) {
			ITree thrownType = builder.createNode("THROWS", thrown.getQualifiedName());
			thrownType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, thrown);
			type.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, thrownType);
			builder.addSiblingNode(thrownType);
		}

		super.visitCtMethod(e);
	}

    @Override
    public void scanCtReference(CtReference reference) {


        if (reference instanceof CtTypeReference && reference.getRoleInParent() == CtRole.SUPER_TYPE) {
            ITree superType = builder.createNode("SUPER_TYPE", reference.toString());
            CtWrapper<CtReference> k = new CtWrapper<CtReference>(reference, reference.getParent());
            superType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, k);
            reference.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, superType);
            builder.addSiblingNode(superType);
        } else {
            super.scanCtReference(reference);
        }
    }

}
