package gumtree.spoon.diff;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Matias Martinez, matias.martinez@inria.fr
 */
public class DiffImpl implements Diff {
	/**
	 * Actions over all tree nodes (CtElements)
	 */
	private final List<Operation> allOperations;
	/**
	 * Actions over the changes roots.
	 */
	private final List<Operation> rootOperations;
	/**
	 * the mapping of this diff
	 */
	private final MappingStore _mappingsComp;
	/**
	 * Context of the spoon diff.
	 */
	private final TreeContext context;

	public DiffImpl(TreeContext context, ITree rootSpoonLeft, ITree rootSpoonRight) {
		final MappingStore mappingsComp = new MappingStore();

		final Matcher matcher = new CompositeMatchers.ClassicGumtree(rootSpoonLeft, rootSpoonRight, mappingsComp);
		matcher.match();

		final ActionGenerator actionGenerator = new ActionGenerator(rootSpoonLeft, rootSpoonRight, matcher.getMappings());
		actionGenerator.generate();

		final ActionClassifier actionClassifier = new ActionClassifier();
		this.allOperations = convertToSpoon(actionGenerator.getActions());
		this.rootOperations = convertToSpoon(actionClassifier.getRootActions(matcher.getMappingSet(), actionGenerator.getActions()));
		this._mappingsComp = mappingsComp;
		this.context = context;
	}

	private List<Operation> convertToSpoon(List<Action> actions) {
		return actions.stream().map(action -> {
			if (action instanceof Insert) {
				return new InsertOperation((Insert) action);
			} else if (action instanceof Delete) {
				return new DeleteOperation((Delete) action);
			} else if (action instanceof Update) {
				return new UpdateOperation((Update) action);
			} else if (action instanceof Move) {
				return new MoveOperation((Move) action);
			} else {
				throw new IllegalArgumentException("Please support the new type " + action.getClass());
			}
		}).collect(Collectors.toList());
	}

	@Override
	public List<Operation> getAllOperations() {
		return Collections.unmodifiableList(allOperations);
	}

	@Override
	public List<Operation> getRootOperations() {
		return Collections.unmodifiableList(rootOperations);
	}

	@Override
	public List<Operation> getOperationChildren(Operation operationParent, List<Operation> rootOperations) {
		return rootOperations.stream() //
				.filter(operation -> operation.getNode().getParent().equals(operationParent)) //
				.collect(Collectors.toList());
	}

	@Override
	public CtElement changedNode() {
		if (rootOperations.size() != 1) {
			throw new IllegalArgumentException("Should have only one root action.");
		}
		return commonAncestor();
	}

	@Override
	public CtElement commonAncestor() {
		final List<CtElement> copy = new ArrayList<>();
		for (Operation operation : rootOperations) {
			CtElement el = operation.getNode();
			if (operation instanceof InsertOperation) {
				// we take the corresponding node in the source tree
				el = (CtElement) _mappingsComp.getSrc(operation.getAction().getNode().getParent()).getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			}
			copy.add(el);
		}
		while (copy.size() >= 2) {
			CtElement first = copy.remove(0);
			CtElement second = copy.remove(0);
			copy.add(commonAncestor(first, second));
		}
		return copy.get(0);
	}

	private CtElement commonAncestor(CtElement first, CtElement second) {
		while (first != null) {
			CtElement el = second;
			while (el != null) {
				if (first == el) {
					return first;
				}
				el = el.getParent();
			}
			first = first.getParent();
		}
		return null;
	}

	@Override
	public CtElement changedNode(Class<? extends Operation> operationWanted) {
		final Optional<Operation> firstNode = rootOperations.stream() //
				.filter(operation -> operationWanted.isAssignableFrom(operation.getClass())) //
				.findFirst();
		if (firstNode.isPresent()) {
			return firstNode.get().getNode();
		}
		throw new NoSuchElementException();
	}

	@Override
	public boolean containsOperation(OperationKind kind, String nodeKind) {
		return rootOperations.stream() //
				.anyMatch(operation -> operation.getAction().getClass().getSimpleName().equals(kind.name()) //
						&& context.getTypeLabel(operation.getAction().getNode()).equals(nodeKind));
	}

	@Override
	public boolean containsOperation(OperationKind kind, String nodeKind, String nodeLabel) {
		return containsOperations(getRootOperations(), kind, nodeKind, nodeLabel);
	}

	@Override
	public boolean containsOperations(List<Operation> operations, OperationKind kind, String nodeKind, String nodeLabel) {
		return operations.stream().anyMatch(operation -> operation.getAction().getClass().getSimpleName().equals(kind.name()) //
				&& context.getTypeLabel(operation.getAction().getNode()).equals(nodeKind)
				&& operation.getAction().getNode().getLabel().equals(nodeLabel)
		);
	}

	@Override
	public void debugInformation() {
		System.err.println(toDebugString());
	}

	private String toDebugString() {
		String result = "";
		for (Operation operation : rootOperations) {
			ITree node = operation.getAction().getNode();
			final CtElement nodeElement = operation.getNode();
			String label = "\"" + node.getLabel() + "\"";
			if (operation instanceof UpdateOperation) {
				label += " to \"" + ((Update) operation.getAction()).getValue() + "\"";
			}
			String nodeType = context.getTypeLabel(node.getType());
			if (nodeElement != null) {
				nodeType += "(" + nodeElement.getClass().getSimpleName()+")";
			}
			result += "\"" + operation.getAction().getClass().getSimpleName() + "\", \"" + nodeType + "\", " + label + " (size: " + node.getDescendants().size() + ")" + node.toTreeString();
		}
		return result;
	}

	@Override
	public String toString() {
		if (rootOperations.size() == 0) {
			return "no AST change";
		}
		final StringBuilder stringBuilder = new StringBuilder();
		final CtElement ctElement = commonAncestor();
		for (Operation operation : rootOperations) {
			stringBuilder.append(toStringAction(operation.getAction()));

			// if all actions are applied on the same node print only the first action
			if (operation.getNode().equals(ctElement) && operation instanceof UpdateOperation) {
				break;
			}
		}
		return stringBuilder.toString();
	}

	private String toStringAction(Action action) {
		String newline = System.getProperty("line.separator");
		StringBuilder stringBuilder = new StringBuilder();

		CtElement element = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		// action name
		stringBuilder.append(action.getClass().getSimpleName());

		// node type
		String nodeType = element.getClass().getSimpleName();
		nodeType = nodeType.substring(2, nodeType.length() - 4);
		stringBuilder.append(" ").append(nodeType);

		// action position
		CtElement parent = element;
		while (parent.getParent() != null && !(parent.getParent() instanceof CtPackage)) {
			parent = parent.getParent();
		}
		String position = " at ";
		if (parent instanceof CtType) {
			position += ((CtType) parent).getQualifiedName();
		}
		if (element.getPosition() != null) {
			position += ":" + element.getPosition().getLine();
		}
		if (action instanceof Move) {
			CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
			position = " from " + element.getParent(CtClass.class).getQualifiedName() + ":" + element.getPosition().getLine();
			position += " to " + elementDest.getParent(CtClass.class).getQualifiedName() + ":" + elementDest.getPosition().getLine();
		}
		stringBuilder.append(position).append(newline);

		// code change
		String label = element.toString();
		if (action instanceof Update) {
			CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
			label += " to " + elementDest.toString();
		}
		String[] split = label.split(newline);
		for (String s : split) {
			stringBuilder.append("\t").append(s).append(newline);
		}
		return stringBuilder.toString();
	}
}
