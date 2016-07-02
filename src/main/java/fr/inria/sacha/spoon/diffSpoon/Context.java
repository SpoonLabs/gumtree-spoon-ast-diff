package fr.inria.sacha.spoon.diffSpoon;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import java.util.ArrayList;
import java.util.List;

public final class Context {
	private final TreeContext treeContext = new TreeContext();
	private final List<String> typesId = new ArrayList<>();

	public int resolveTypeId(String typeClass) {
		if (!typesId.contains(typeClass)) {
			typesId.add(typeClass);
		}
		return typesId.indexOf(typeClass);
	}

	public boolean nodeIsKindOf(ITree node, String nodeKind) {
		return treeContext.getTypeLabel(node).equals(nodeKind);
	}

	public ITree createTree(int type, String label, String typeLabel) {
		return treeContext.createTree(type, label, typeLabel);
	}
}