package gumtree.spoon.builder;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import spoon.SpoonException;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.meta.RoleHandler;
import spoon.reflect.meta.impl.RoleHandlerHelper;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.CtScanner;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class TreeScanner extends CtScanner {
	public static final String NOTYPE = "<notype>";
	private final TreeContext treeContext;
	private final Stack<ITree> nodes = new Stack<>();

	TreeScanner(TreeContext treeContext, ITree root) {
		this.treeContext = treeContext;
		nodes.push(root);
	}
	
	@Override
	public void scan(CtRole role, CtElement element) {
		if (element == null) {
			return;
		}
		ITree node = createNode(role, element, null);
		pushNodeToTree(node);
		//add primitive attributes as first child nodes
		List<RoleHandler> handlers = RoleHandlerHelper.getRoleHandlers(element.getClass());
		for (RoleHandler roleHandler : handlers) {
			if (CtElement.class.isAssignableFrom(roleHandler.getValueClass())) {
				continue;
			}
			if (ignoredRoles.contains(roleHandler.getRole())) {
				continue;
			}
			//it is primitive role, which doesn't hold CtElement
			Collection<Object>  values = roleHandler.asCollection(element);
			for (Object object : values) {
				if (object != null) {
					node.addChild(createNode(roleHandler.getRole(), object, getPrimitiveLabel(object)));
				}
			}
		}
		try {
			super.scan(role, element);
		} finally {
			nodes.pop();
		}
	}
	
	private static final Set<CtRole> ignoredRoles = new HashSet<>();
	static {
		ignoredRoles.add(CtRole.POSITION);
	}

	private void pushNodeToTree(ITree node) {
		ITree parent = nodes.peek();
		if (parent != null) { // happens when nodes.push(null)
			parent.addChild(node);
		}
		nodes.push(node);
	}

	private ITree createNode(CtRole roleInParent, Object element, String label) {
		ITree newNode = createNode(roleInParent, label);
		newNode.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, element);
		return newNode;
	}

	private ITree createNode(CtRole role, String label) {
		if (role == null) {
			return treeContext.createTree(-2, label, "ROOT");
		}
		return treeContext.createTree(role.ordinal(), label, role.name());
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
