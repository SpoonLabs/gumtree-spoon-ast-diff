package gumtree.spoon.builder;

import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.visitor.CtInheritanceScanner;

class NodeCreator extends CtInheritanceScanner {
	private final TreeScanner builder;

	NodeCreator(TreeScanner builder) {
		this.builder = builder;
	}

	@Override
	public void scanCtNamedElement(CtNamedElement e) {
		builder.addNodeToTree(builder.createNode(e, e.getSimpleName()));
	}

	@Override
	public <T, E extends CtExpression<?>> void scanCtArrayAccess(CtArrayAccess<T, E> arrayAccess) {
		builder.addNodeToTree(builder.createNode(arrayAccess, arrayAccess.toString()));
	}

	@Override
	public <T> void scanCtVariableAccess(CtVariableAccess<T> variableAccess) {
		if (variableAccess.getVariable() != null) {
			builder.addNodeToTree(builder.createNode(variableAccess, variableAccess.getVariable().getSimpleName()));
		}
	}

	@Override
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		if (invocation.getExecutable() != null) {
			builder.addNodeToTree(builder.createNode(invocation, invocation.getExecutable().getSignature()));
		}
	}

	@Override
	public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
		if (ctConstructorCall.getExecutable() != null) {
			builder.addNodeToTree(builder.createNode(ctConstructorCall, ctConstructorCall.getExecutable().getSignature()));
		}
	}

	@Override
	public <T> void visitCtCatchVariable(CtCatchVariable<T> e) {
		builder.addNodeToTree(builder.createNode(e, e.getType().getQualifiedName()));
	}

	@Override
	public <T> void visitCtLiteral(CtLiteral<T> literal) {
		builder.addNodeToTree(builder.createNode(literal, literal.toString()));
	}

	@Override
	public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
		builder.addNodeToTree(builder.createNode(operator, operator.getKind().toString()));
	}

	@Override
	public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {
		builder.addNodeToTree(builder.createNode(operator, operator.getKind().toString()));
	}

	@Override
	public <T> void visitCtThisAccess(CtThisAccess<T> thisAccess) {
		builder.addNodeToTree(builder.createNode(thisAccess, thisAccess.toString()));
	}

	@Override
	public <T> void visitCtTypeAccess(CtTypeAccess<T> typeAccess) {
		builder.addNodeToTree(builder.createNode(typeAccess, typeAccess.getAccessedType().getQualifiedName()));
	}
}
