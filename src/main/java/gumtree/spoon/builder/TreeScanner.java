/* *****************************************************************************
 * Copyright 2016 Matias Martinez
 * Copyright (c) 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************/

package gumtree.spoon.builder;

import static com.github.gumtreediff.tree.TypeSet.type;

import java.util.Stack;

import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.Type;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.CtScanner;

public class TreeScanner extends CtScanner {
	public static final String NOTYPE = "<notype>";
	private final TreeContext treeContext;
	private final Stack<Tree> nodes = new Stack<>();
	boolean nolabel = false;

	TreeScanner(TreeContext treeContext, Tree root) {
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

		if (element.getParent() != null && element.getParent() instanceof CtInvocation &&
				((CtInvocation<?>)element.getParent()).getTarget() != null &&
				((CtInvocation<?>)element.getParent()).getTarget().equals(element)) {
				//Wrap invocation targets
			Tree node = createNode("Target" + "(" + nodeTypeName + ")", element, label);

			CtTargetWrapper targetWrapper = new CtTargetWrapper(element, element.getParent());
			targetWrapper.setPosition(element.getPosition());
			node.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, targetWrapper);
			pushNodeToTree(node);
		} else {
			pushNodeToTree(createNode(nodeTypeName, element, label));
		}

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

		// We need to ignore the literal in the annotation value because we store them in CtWrapper
		if (element instanceof CtLiteral<?> && element.getParent() instanceof CtAnnotation) {
			return true;
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

	private void pushNodeToTree(Tree node) {
		Tree parent = nodes.peek();
		if (parent != null) { // happens when nodes.push(null)
			parent.addChild(node);
		}
		nodes.push(node);
	}

	void addSiblingNode(Tree node) {
		Tree parent = nodes.peek();
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
		if (element.getRoleInParent() == CtRole.SUPER_TYPE) {
			nodeTypeName = "SUPER_TYPE";
		}
		return nodeTypeName;
	}

	private Tree createNode(String nodeTypeName, CtElement element, String label) {

		Tree newNode = createNode(nodeTypeName, label);
		newNode.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, element);
		element.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, newNode);
		return newNode;
	}

	private String getTypeName(String simpleName) {
		// Removes the "Ct" at the beginning and the "Impl" at the end.
		return simpleName.substring(2, simpleName.length() - 4);
	}

	public Tree createNode(String typeClass, String label) {

		Type type = type(typeClass);

		return treeContext.createTree(type, label);
	}
}
