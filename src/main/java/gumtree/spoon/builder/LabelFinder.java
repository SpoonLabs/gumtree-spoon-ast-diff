package gumtree.spoon.builder;

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
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.CtInheritanceScanner;
import spoon.reflect.visitor.CtScanner;

class LabelFinder extends CtInheritanceScanner {
	public String label = "";

	@Override
	public void scanCtNamedElement(CtNamedElement e) {
		label = e.getSimpleName();
	}

	@Override
	public <T> void scanCtVariableAccess(CtVariableAccess<T> variableAccess) {
		label = variableAccess.getVariable().getSimpleName();
	}


	@Override
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		if (invocation.getExecutable() != null) {
			label = invocation.getExecutable().getSignature();
		}
	}

	@Override
	public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
		if (ctConstructorCall.getExecutable() != null) {
			label = ctConstructorCall.getExecutable().getSignature();
		}
	}

	@Override
	public <T> void visitCtLiteral(CtLiteral<T> literal) {
		label = literal.toString();
	}

	@Override
	public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
		label = operator.getKind().toString();
	}

	@Override
	public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {
		label = operator.getKind().toString();
	}

	@Override
	public <T> void visitCtThisAccess(CtThisAccess<T> thisAccess) {
		label =  thisAccess.toString();
	}

	@Override
	public <T> void visitCtTypeAccess(CtTypeAccess<T> typeAccess) {
		label = typeAccess.getAccessedType().getQualifiedName();
	}
}
