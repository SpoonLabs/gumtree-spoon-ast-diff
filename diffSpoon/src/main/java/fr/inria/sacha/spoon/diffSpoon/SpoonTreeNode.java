package fr.inria.sacha.spoon.diffSpoon;

import spoon.reflect.declaration.CtElement;
import fr.labri.gumtree.Tree;
/**
 * 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class SpoonTreeNode extends Tree {

	CtElement element = null;
	
	public SpoonTreeNode(int type) {
		super(type);
		
	}

}
