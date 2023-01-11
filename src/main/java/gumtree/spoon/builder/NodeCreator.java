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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.github.gumtreediff.tree.Tree;

import spoon.reflect.code.CtExpression;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtActualTypeContainer;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtInheritanceScanner;
import spoon.support.reflect.CtExtendedModifier;
import spoon.support.reflect.cu.position.SourcePositionImpl;

/**
 * responsible to add additional nodes only overrides scan* to add new nodes
 */
public class NodeCreator extends CtInheritanceScanner {
	public static final String MODIFIERS = "Modifiers_";
	private final TreeScanner builder;

	NodeCreator(TreeScanner builder) {
		this.builder = builder;
	}

	@Override
	public void scanCtModifiable(CtModifiable m) {

		if (m.getModifiers().isEmpty())
			return;

		// We add the type of modifiable element
		String type = MODIFIERS + getClassName(m.getClass().getSimpleName());
		Tree modifiers = builder.createNode(type, "");

		// ensuring an order (instead of hashset)
		// otherwise some flaky tests in CI
		Set<CtExtendedModifier> modifiers1 = new TreeSet<>(Comparator.comparing(o -> o.getKind().name()));
		modifiers1.addAll(m.getExtendedModifiers());

		// We create a virtual node
		CtVirtualElement virtualElement = new CtVirtualElement(type, m, m.getModifiers(), CtRole.MODIFIER);
		modifiers.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, virtualElement);

		List<SourcePosition> modifierPositions = new ArrayList<>();

		for (CtExtendedModifier mod : modifiers1) {
			Tree modifier = builder.createNode("Modifier", mod.getKind().toString());
			modifiers.addChild(modifier);
			// We wrap the modifier's kind (which is not a CtElement)
			CtWrapper wrapper = new CtWrapper<>(mod.getKind(), m, CtRole.MODIFIER);
			wrapper.setPosition(mod.getPosition());
			modifierPositions.add(mod.getPosition());
			modifier.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, wrapper);
		}

		virtualElement.setPosition(computeSourcePositionOfVirtualElement(modifierPositions));
		builder.addSiblingNode(modifiers);
	}

	private static SourcePosition computeSourcePositionOfVirtualElement(List<SourcePosition> modifierPositions) {
		CompilationUnit cu = null;
		Integer sourceStart = null;
		Integer sourceEnd = null;
		for (SourcePosition position : modifierPositions) {
			if (position instanceof NoSourcePosition) { return SourcePosition.NOPOSITION; }
			if (sourceStart == null || position.getSourceStart() < sourceStart) {
				sourceStart = position.getSourceStart();
			}
			if (sourceEnd == null || position.getSourceEnd() > sourceEnd) {
				sourceEnd = position.getSourceEnd();
			}
			if (cu == null) { cu = position.getCompilationUnit(); }
		}
		return new SourcePositionImpl(cu, sourceStart, sourceEnd, cu.getLineSeparatorPositions());
	}

	private String getClassName(String simpleName) {
		if (simpleName == null)
			return "";
		return simpleName.replace("Ct", "").replace("Impl", "");
	}

	@Override
	public <T> void scanCtVariable(CtVariable<T> e) {
		CtTypeReference<T> type = e.getType();
		if (type != null) {
			Tree variableType = builder.createNode("VARIABLE_TYPE", type.getQualifiedName());
			variableType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, type);
			type.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, variableType);
			computeTreeOfTypeReferences(type, variableType);
			builder.addSiblingNode(variableType);
		}
	}

	@Override
	public void scanCtActualTypeContainer(CtActualTypeContainer reference) {
		for (CtTypeReference<?> ctTypeArgument : reference.getActualTypeArguments()) {
			Tree typeArgument = builder.createNode("TYPE_ARGUMENT", ctTypeArgument.getQualifiedName());
			typeArgument.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, ctTypeArgument);
			ctTypeArgument.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, typeArgument);
			computeTreeOfTypeReferences(ctTypeArgument, typeArgument);
			builder.addSiblingNode(typeArgument);
		}
	}

	@Override
	public <T> void scanCtTypeInformation(CtTypeInformation typeReference) {
		if (typeReference.getSuperInterfaces().isEmpty()) {
			return;
		}

		// create the root super interface node whose children will be *actual* spoon
		// nodes of interfaces
		Tree superInterfaceRoot = builder.createNode("SUPER_INTERFACES", "");
		String virtualNodeDescription = "SuperInterfaces_" + typeReference.getQualifiedName();
		superInterfaceRoot.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, new CtVirtualElement(virtualNodeDescription,
				(CtElement) typeReference, typeReference.getSuperInterfaces(), CtRole.INTERFACE));

		// attach each super interface to the root created above
		for (CtTypeReference<?> superInterface : typeReference.getSuperInterfaces()) {
			Tree superInterfaceNode = builder.createNode("INTERFACE", superInterface.getQualifiedName());
			superInterfaceNode.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, superInterface);
			superInterface.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, superInterfaceNode);
			superInterfaceRoot.addChild(superInterfaceNode);
			computeTreeOfTypeReferences(superInterface, superInterfaceNode);
		}
		builder.addSiblingNode(superInterfaceRoot);
	}

	/**
	 * Creates a tree of nested type references where each nested type reference is
	 * a child of its container.
	 */
	private void computeTreeOfTypeReferences(CtTypeReference<?> type, Tree parentType) {
		for (CtTypeReference<?> ctTypeArgument : type.getActualTypeArguments()) {
			Tree typeArgument = builder.createNode("TYPE_ARGUMENT", ctTypeArgument.getQualifiedName());
			typeArgument.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, ctTypeArgument);
			ctTypeArgument.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, typeArgument);
			parentType.addChild(typeArgument);
			computeTreeOfTypeReferences(ctTypeArgument, typeArgument);
		}
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> e) {
		// add the return type of the method
		CtTypeReference<T> type = e.getType();
		if (type != null) {
			Tree returnType = builder.createNode("RETURN_TYPE", type.getQualifiedName());
			returnType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, type);
			type.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, returnType);
			computeTreeOfTypeReferences(type, returnType);
			builder.addSiblingNode(returnType);
		}

		if (!e.getThrownTypes().isEmpty()) {
			Tree thrownTypeRoot = builder.createNode("THROWN_TYPES", "");
			String virtualNodeDescription = "ThrownTypes_" + e.getSimpleName();
			thrownTypeRoot.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT,
					new CtVirtualElement(virtualNodeDescription, e, e.getThrownTypes(), CtRole.THROWN));

			for (CtTypeReference<? extends Throwable> thrownType : e.getThrownTypes()) {
				Tree thrownNode = builder.createNode("THROWN", thrownType.getQualifiedName());
				thrownNode.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, thrownType);
				thrownType.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, thrownNode);
				thrownTypeRoot.addChild(thrownNode);
			}
			builder.addSiblingNode(thrownTypeRoot);
		}
		super.visitCtMethod(e);
	}

	@Override
	public <A extends Annotation> void visitCtAnnotation(CtAnnotation<A> annotation) {
		if (annotation.getValues().isEmpty()) {
			return;
		}

		final String virtualNodeDescription = "AnnotationValues_" + getClassName(annotation.getClass().getSimpleName());
		Tree annotationNode = builder.createNode(virtualNodeDescription, "");

		annotationNode.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT,
				new CtVirtualElement(virtualNodeDescription, annotation, annotation.getValues().entrySet(), CtRole.VALUE));

		for (Map.Entry<String, CtExpression> entry: annotation.getValues().entrySet()) {
			Tree annotationValueNode = builder.createNode("ANNOTATION_VALUE", entry.toString());
			annotationNode.addChild(annotationValueNode);
			CtWrapper wrapper = new CtWrapper(entry, annotation, CtRole.VALUE);
			wrapper.setPosition(entry.getValue().getPosition());
			annotationValueNode.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, wrapper);
		}
		builder.addSiblingNode(annotationNode);
	}

	@Override
	public <T> void scanCtTypedElement(CtTypedElement<T> e) {
		if (e instanceof CtExpression) {
			CtExpression<?> expression = (CtExpression<?>) e;

			for (CtTypeReference<?> ctTypeCast : expression.getTypeCasts()) {
				Tree typeCast = builder.createNode("TYPE_CAST", ctTypeCast.getQualifiedName());
				typeCast.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, ctTypeCast);
				ctTypeCast.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, typeCast);
				computeTreeOfTypeReferences(ctTypeCast, typeCast);
				builder.addSiblingNode(typeCast);
			}
		}
	}
}
