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

package fr.inria.sacha.spoon.diffSpoon;

import com.github.gumtreediff.tree.ITree;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.CtInheritanceScanner;
import spoon.reflect.visitor.CtScanner;

import java.util.Stack;

import static fr.inria.sacha.spoon.diffSpoon.utils.TreeUtil.createNode;

/**
 * Scanner to create a GumTree's Tree representation for a Spoon CtClass.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 */
public class SpoonGumTreeBuilder extends CtScanner {
	public static final String SPOON_OBJECT = "spoon_object";
	public static final String SPOON_OBJECT_DEST = "spoon_object_dest";

	private final Stack<ITree> nodes = new Stack<>();
	private final Context context;
	private final ITree root;

	public SpoonGumTreeBuilder(Context context) {
		super();
		this.context = context;
		root = context.createTree(-1, "", "root");
		nodes.push(root);
	}

	@Override
	public void enter(CtElement element) {
		if (element instanceof CtReference) {
			nodes.push(null);
			return;
		}

		int size = nodes.size();
		new NodeCreator().scan(element);
		if (size == nodes.size()) {
			addNodeToTree(createNode("", element, this.context));
		}
	}

	@Override
	public void exit(CtElement element) {
		nodes.pop();
		super.exit(element);
	}

	private void addNodeToTree(ITree node) {
		ITree parent = nodes.peek();
		if (parent != null) {// happens when nodes.push(null)
			parent.addChild(node);
		}
		nodes.push(node);
	}

	public ITree getRoot() {
		return root;
	}

	private class NodeCreator extends CtInheritanceScanner {
		@Override
		public void scanCtNamedElement(CtNamedElement e) {
			addNodeToTree(createNode(e.getSimpleName(), e, context));
		}

		@Override
		public <T, E extends CtExpression<?>> void scanCtArrayAccess(CtArrayAccess<T, E> arrayAccess) {
			addNodeToTree(createNode(arrayAccess.toString(), arrayAccess, context));
		}

		@Override
		public <T> void scanCtVariableAccess(CtVariableAccess<T> variableAccess) {
			if (variableAccess.getVariable() != null) {
				addNodeToTree(createNode(variableAccess.getVariable().getSimpleName(), variableAccess, context));
			}
		}

		@Override
		public <T> void visitCtInvocation(CtInvocation<T> invocation) {
			if (invocation.getExecutable() != null) {
				addNodeToTree(createNode(invocation.getExecutable().getSignature(), invocation, context));
			}
		}

		@Override
		public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
			if (ctConstructorCall.getType() != null) {
				addNodeToTree(createNode(ctConstructorCall.getExecutable().getSignature(), ctConstructorCall, context));
			}
		}

		@Override
		public <T> void visitCtLiteral(CtLiteral<T> literal) {
			addNodeToTree(createNode(literal.toString(), literal, context));
		}

		@Override
		public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
			addNodeToTree(createNode(operator.getKind().toString(), operator, context));
		}

		@Override
		public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {
			addNodeToTree(createNode(operator.getKind().toString(), operator, context));
		}

		@Override
		public <T> void visitCtThisAccess(CtThisAccess<T> thisAccess) {
			addNodeToTree(createNode(thisAccess.toString(), thisAccess, context));
		}

		@Override
		public <T> void visitCtTypeAccess(CtTypeAccess<T> typeAccess) {
			addNodeToTree(createNode(typeAccess.getSignature(), typeAccess, context));
		}
	}
}
