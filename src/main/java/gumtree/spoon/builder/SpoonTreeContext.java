/*
 * Spoon - http://spoon.gforge.inria.fr/
 * Copyright (C) 2006 INRIA Futurs <renaud.pawlak@inria.fr>
 *
 * This software is governed by the CeCILL-C License under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as
 * circulated by CEA, CNRS and INRIA at http://www.cecill.info.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the CeCILL-C License for more details.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package gumtree.spoon.builder;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

/**
 * Extension of Tree, which maps to node or attribute value of Spoon AST
 */
public class SpoonTreeContext extends TreeContext {
	
	public ITree createTree(int type, String label, String typeLabel, Object value) {
        registerTypeLabel(type, typeLabel);
        return new SpoonTree(type, label, value);
	}
	
	@Override
	public ITree createTree(int type, String label, String typeLabel) {
		throw new UnsupportedOperationException();
	}
	
}