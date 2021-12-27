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

import static com.github.gumtreediff.tree.TypeSet.type;

import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.Type;

import spoon.reflect.declaration.CtElement;

/**
 * Scanner to create a GumTree's Tree representation for a Spoon CtClass.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 */
public class SpoonGumTreeBuilder {
	public static final String SPOON_OBJECT = "spoon_object";
	public static final String SPOON_OBJECT_DEST = "spoon_object_dest";
	public static final String GUMTREE_NODE = "gtnode";

	private final TreeContext treeContext = new TreeContext();

	public Tree getTree(CtElement element) {
		Type type = type("root");
		final Tree root = treeContext.createTree(type, "");
		new TreeScanner(treeContext, root).scan(element);
		return root;
	}

	public TreeContext getTreeContext() {
		return treeContext;
	}
}
