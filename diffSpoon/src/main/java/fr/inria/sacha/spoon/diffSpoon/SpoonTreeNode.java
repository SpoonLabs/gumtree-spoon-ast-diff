package fr.inria.sacha.spoon.diffSpoon;

import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
@Deprecated
public class SpoonTreeNode extends fr.labri.gumtree.tree.Tree {

	CtElement element = null;
	
	public SpoonTreeNode(int type) {
		super(type);
		
	}

}
