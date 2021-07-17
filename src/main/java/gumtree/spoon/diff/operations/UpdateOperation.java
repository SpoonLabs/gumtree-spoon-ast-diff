package gumtree.spoon.diff.operations;

import com.github.gumtreediff.actions.model.Update;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;

public class UpdateOperation extends Operation<Update> {
	private final CtElement destElement;
	private final CtRole role;

	public UpdateOperation(Update action) {
		super(action);
		destElement = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
		if (destElement instanceof CtBinaryOperator<?>) {
			role = CtRole.OPERATOR_KIND;
		}
		else if (destElement instanceof CtInvocation<?>) {
			role = CtRole.EXECUTABLE_REF;
		}
		else if (destElement instanceof CtLiteral<?>) {
			role = CtRole.VALUE;
		}
		else {
			role = CtRole.NAME;;
		}
	}

	@Override
	public CtElement getDstNode() {
		return destElement;
	}

	public CtRole getRole() {
		return role;
	}

}
