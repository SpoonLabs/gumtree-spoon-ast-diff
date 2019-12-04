package gumtree.spoon.builder;

import java.util.Stack;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.CtScanner;

public class TreeScanner extends CtScanner {
	public static final String NOTYPE = "<notype>";
	private final TreeContext treeContext;
	private final Stack<ITree> nodes = new Stack<>();
	boolean nolabel = false;

	TreeScanner(TreeContext treeContext, ITree root) {
		this.treeContext = treeContext;
		nodes.push(root);
		nolabel = isNoLabelMode();
	}

	@Override
	public void enter(CtElement element) {
		if (isToIgnore(element)) {
			super.enter(element);
			return;
		}

		String label = null;
		String nodeTypeName = getNodeType(element);

		if (nolabel)
			label = nodeTypeName;
		else {
			LabelFinder lf = new LabelFinder();
			lf.scan(element);
			label = lf.label;
		}
		pushNodeToTree(createNode(nodeTypeName, element, label));

		int depthBefore = nodes.size();

		new NodeCreator(this).scan(element);

		if (nodes.size() != depthBefore) {
			// contract: this should never happen
			throw new RuntimeException("too many nodes pushed");
		}
	}

	public boolean isNoLabelMode() {
		String nolabel = System.getProperty("nolabel");
		if (nolabel != null) {
			return Boolean.valueOf(nolabel);
		}
		return false;
	}

	/**
	 * Ignore some element from the AST
	 * 
	 * @param element
	 * @return
	 */
	private boolean isToIgnore(CtElement element) {
		if (element instanceof CtStatementList && !(element instanceof CtCase)) {
			if (element.getRoleInParent() == CtRole.ELSE || element.getRoleInParent() == CtRole.THEN) {
				return false;
			}
			return true;
		}

		if (element instanceof CtReference && element.getRoleInParent() == CtRole.SUPER_TYPE) {
			return false;
		}

		return element.isImplicit() || element instanceof CtReference;
	}

	@Override
	public void exit(CtElement element) {
		if (!isToIgnore(element)) {
			nodes.pop();
		}
		super.exit(element);
	}

	private void pushNodeToTree(ITree node) {
		ITree parent = nodes.peek();
		if (parent != null) { // happens when nodes.push(null)
			parent.addChild(node);
		}
		nodes.push(node);
	}

	void addSiblingNode(ITree node) {
		ITree parent = nodes.peek();
		if (parent != null) { // happens when nodes.push(null)
			parent.addChild(node);
		}
	}

	private String getNodeType(CtElement element) {
		String nodeTypeName = NOTYPE;
		if (element != null) {
			nodeTypeName = getTypeName(element.getClass().getSimpleName());
		}
		if (element instanceof CtBlock) {
			nodeTypeName = element.getRoleInParent().toString();
		}
		return nodeTypeName;
	}

	private ITree createNode(String nodeTypeName, CtElement element, String label) {

		ITree newNode = createNode(nodeTypeName, label);
		newNode.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, element);
		element.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, newNode);
		return newNode;
	}

	private String getTypeName(String simpleName) {
		// Removes the "Ct" at the beginning and the "Impl" at the end.
		return simpleName.substring(2, simpleName.length() - 4);
	}

	public ITree createNode(String typeClass, String label) {
		return treeContext.createTree(typeClass.hashCode(), label, typeClass);
	}
}
