package gumtree.spoon.builder;

import spoon.reflect.code.CtAssert;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtContinue;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtInheritanceScanner;

import java.lang.annotation.Annotation;

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
		if (invocation.getExecutable().isConstructor()) {
			CtType<?> parentType = invocation.getParent(CtType.class);
			if (parentType.getQualifiedName().equals(invocation.getExecutable().getDeclaringType().getQualifiedName())) {
				label = "this";
			} else {
				label = "super";
			}
		} else {
			label = invocation.getExecutable().getSimpleName();
		}
	}

	@Override
	public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
		label = ctConstructorCall.getExecutable().getSignature();
	}

	@Override
	public <T> void visitCtLiteral(CtLiteral<T> literal) {
		label = literal.toString();
	}

	@Override
	public void visitCtIf(CtIf e) {
		label = "if";
	}

	@Override
	public void visitCtWhile(CtWhile e) {
		label = "while";
	}

	@Override
	public void visitCtBreak(CtBreak e) {
		label = "break";
	}

	@Override
	public void visitCtContinue(CtContinue e) {
		label = "continue";
	}

	@Override
	public <R> void visitCtReturn(CtReturn<R> e) {
		label = "return";
	}

	@Override
	public <T> void visitCtAssert(CtAssert<T> e) {
		label = "assert";
	}

	@Override
	public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> e) {
		label = "=";
	}

	@Override
	public <T, A extends T> void visitCtOperatorAssignment(CtOperatorAssignment<T, A> e) {
		label = e.getLabel();
	}

	@Override
	public <R> void visitCtBlock(CtBlock<R> e) {
		if (e.getRoleInParent() == CtRole.ELSE) {
			label = "ELSE";
		} else if (e.getRoleInParent() == CtRole.THEN) {
			label = "THEN";
		} else {
			label = "{";
		}
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
		label = thisAccess.toString();
	}

	@Override
	public <T> void visitCtSuperAccess(CtSuperAccess<T> f) {
		label = f.toString();
	}

	@Override
	public <T> void visitCtTypeAccess(CtTypeAccess<T> typeAccess) {
		if (typeAccess.getAccessedType() != null) {
			label = typeAccess.getAccessedType().getQualifiedName();
		}
	}

	@Override
	public void visitCtComment(CtComment comment) {
		label = comment.getContent();
	}

	@Override
	public <T extends Annotation> void visitCtAnnotation(CtAnnotation<T> annotation) {
		label = annotation.getType().getQualifiedName();
	}

	@Override
	public <T> void visitCtTypeReference(CtTypeReference<T> e) {
			label = e.getQualifiedName();
	}
}
