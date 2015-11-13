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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.CtScanner;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;


/**
 * Scanner to create a GumTree's Tree representation for a Spoon CtClass.
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class SpoonGumTreeBuilder extends CtScanner {

	public static final String SPOON_OBJECT = "spoon_object";
	public static final String SPOON_OBJECT_DEST = "spoon_object_dest";

	public Stack<ITree> nodes;

	public ITree root;

	public static List<String> typesId = new ArrayList<String>();

	
	public SpoonGumTreeBuilder() {
		super();
		init();
	}
	
	public void init() {
		nodes = new Stack<ITree>();
		root = gtContext.createTree(-1, "", "root");
		nodes.push(root);
	}

	@SuppressWarnings("rawtypes")
	private void createNode(CtElement obj) {

		String label = "";
		String type = getTypeName(obj.getClass().getSimpleName());
		int id = revolveTypeId(obj);

		if (obj instanceof CtInvocation ) {
			CtInvocation inv = (CtInvocation) obj;
			if (inv.getExecutable() == null) {

			} else {
				label = inv.getExecutable().getSimpleName();
			}
		} else 	if (obj instanceof CtConstructorCall ) {
			CtConstructorCall inv = (CtConstructorCall) obj;
			//workaround, the alternative that follows should be better
			label = inv.getType().toString();
			
			// bug in some case 
			/*if (inv.getExecutable() != null) {
				label = inv.getExecutable().getDeclaringType().getSimpleName();
			}*/
		} else if (obj instanceof CtNamedElement) {
			label = ((CtNamedElement) obj).getSimpleName();
		} else if (obj instanceof CtLiteral) {
			label = obj.toString();
		} else if (obj instanceof CtVariableAccess) {
			CtVariableAccess va = (CtVariableAccess) obj;
			// if is a workaround for some bug in noclasspath
			if (va.getVariable()!=null) {
				label = va.getVariable().getSimpleName();
			} 
		}
		else if (obj instanceof CtBinaryOperator) {
			CtBinaryOperator bin = (CtBinaryOperator) obj;
			label = bin.getKind().toString();
		} else if (obj instanceof CtUnaryOperator) {
			CtUnaryOperator bin = (CtUnaryOperator) obj;
			label = bin.getKind().toString();
		} else if (obj instanceof CtStatement) {
			label = "";
		} else if (obj instanceof CtAnonymousExecutable) {
			label = "";
		} else if (obj instanceof CtThisAccess) {
			label = obj.toString();
		} else if (obj instanceof CtConditional || obj instanceof CtNewArray) {
			label = "";
		} else if (obj instanceof CtArrayAccess) {
			label = obj.toString();
		} else {
			label = "";
		}

		ITree newNode = createNode(label, type, id);
		newNode.setMetadata(SPOON_OBJECT, obj);
	}
	/**
	 * Removes the "Ct" at the beginning and the "Impl" at the end
	 * @param simpleName
	 * @return
	 */
	public String getTypeName(String simpleName) {
		
		return simpleName.substring(2, simpleName.length()-4);
	}
	
	TreeContext gtContext = new TreeContext();

	private ITree createNode(String label, String typeLabel, int typeId) {
		
		ITree node = gtContext.createTree(typeId, label, typeLabel);

		nodes.peek().addChild(node);
		nodes.push(node);
		
		return node;
	}

	
	@Override
	public void enter(CtElement element) {
		createNode(element);
	}

	@Override
	public void enterReference(CtReference e) {

		/*
		 * createNode(e); super.enterReference(e);
		 */

	}

	@Override
	public void exitReference(CtReference e) {
		/*
		 * if (e instanceof CtTypeReference) { return; } else
		 */
		/*
		 * nodes.pop(); super.exitReference(e);
		 */

	}

	@Override
	public void exit(CtElement element) {
		nodes.pop();
		super.exit(element);
	}

	public ITree getRoot() {
		return root;
	}

	public int revolveTypeId(Object e) {
		String typeClass = e.getClass().getCanonicalName();
		return resolveTypeId(typeClass);
	}

	public int resolveTypeId(String typeClass) {
		if (!typesId.contains(typeClass)) {
			typesId.add(typeClass);
		}
		return typesId.indexOf(typeClass);
	}
}
