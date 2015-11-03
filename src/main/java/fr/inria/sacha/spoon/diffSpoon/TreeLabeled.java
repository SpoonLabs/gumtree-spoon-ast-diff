package fr.inria.sacha.spoon.diffSpoon;

import fr.labri.gumtree.tree.Tree;


/**
 * 
 * @author Matias Martinez
 *
 */
public class TreeLabeled extends Tree {

	String nodeContent = null;
	
	Object nodeComplement;
	
	public TreeLabeled(int type) {
		super(type);
	}

	public TreeLabeled(int type, String label, String simpleName) {
		super(type, label, simpleName);
	}

	public String getNodeContent() {
		return nodeContent;
	}

	public void setNodeContent(String nodeContent) {
		this.nodeContent = nodeContent;
	}

	public Object getNodeComplement() {
		return nodeComplement;
	}

	public void setNodeComplement(Object node) {
		this.nodeComplement = node;
	}

	
	
}
