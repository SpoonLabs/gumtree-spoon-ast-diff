package gumtree.spoon.builder;

import com.github.gumtreediff.tree.ITree;

import spoon.SpoonException;
import spoon.metamodel.Metamodel;
import spoon.metamodel.MetamodelConcept;
import spoon.metamodel.MetamodelProperty;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class TreeScanner extends CtScanner {
	public static final String NOTYPE = "<notype>";
	private final SpoonTreeContext treeContext;
	private final Stack<ITree> nodes = new Stack<>();

	TreeScanner(SpoonTreeContext treeContext, ITree root) {
		this.treeContext = treeContext;
		nodes.push(root);
	}
	
	@Override
	public void scan(CtRole role, CtElement element) {
		if (element == null || element.isImplicit()) {
			return;
		}
		String elementLabel = null;
		boolean scanChildren = true;
		if (element instanceof CtTypeReference) {
			CtTypeReference<?> typeRef = (CtTypeReference<?>) element;
			elementLabel = typeRef.toString();
			scanChildren = false;
		}
		ITree node = createNode(role, element, elementLabel);
		pushNodeToTree(node);
		try {
			if (scanChildren) {
				//add primitive attributes as first child nodes
				MetamodelConcept mmc = Metamodel.getInstance().getConcept(element.getClass());
				for (MetamodelProperty property : mmc.getProperties()) {
					if (CtElement.class.isAssignableFrom(property.getTypeofItems().getActualClass())) {
						continue;
					}
					if (property.getRole() == CtRole.MODIFIER) {
						this.getClass();
					}
					if (ignoredRoles.contains(property.getRole()) || property.isDerived()) {
						continue;
					}
					//it is primitive role, which doesn't hold CtElement
					Collection<Object>  values = property.getRoleHandler().asCollection(element);
					for (Object object : values) {
						if (object != null) {
							node.addChild(createNode(property.getRole(), object, getPrimitiveLabel(object)));
						}
					}
				}
				super.scan(role, element);
			}
		} finally {
			nodes.pop();
		}
	}
	
	private static final Set<CtRole> ignoredRoles = new HashSet<>(Arrays.asList(CtRole.POSITION, CtRole.IS_IMPLICIT, CtRole.IS_SHADOW));

	private void pushNodeToTree(ITree node) {
		ITree parent = nodes.peek();
		if (parent != null) { // happens when nodes.push(null)
			parent.addChild(node);
		}
		nodes.push(node);
	}

	private ITree createNode(CtRole roleInParent, Object element, String label) {
		if (roleInParent == null) {
			return treeContext.createTree(-2, label, "ROOT", element);
		}
		return treeContext.createTree(roleInParent.ordinal(), label, roleInParent.name(), element);
	}
	
	private String getPrimitiveLabel(Object value) {
		if (value instanceof String) {
			return (String) value;
		}
		if (value instanceof Boolean) {
			return ((Boolean) value).toString();
		}
//		if (value instanceof Number) {
//			return ((Number) value).toString();
//		}
		if (value instanceof Enum) {
			return ((Enum) value).name();
		}
		throw new SpoonException("Unsupported primitive value of class " + value.getClass().getName());
	}
}
