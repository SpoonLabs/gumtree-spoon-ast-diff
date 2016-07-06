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
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

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
		final List<Operation> operations = new ArrayList<>(actions.size());
		for (Action action : actions) {
			if (action instanceof Insert) {
				operations.add(new InsertOperation((Insert) action));
			} else if (action instanceof Delete) {
				operations.add(new DeleteOperation((Delete) action));
			} else if (action instanceof Update) {
				operations.add(new UpdateOperation((Update) action));
			} else if (action instanceof Move) {
				operations.add(new MoveOperation((Move) action));
			} else {
				throw new IllegalArgumentException("Please support the new type " + action.getClass());
			}
		}
		return operations;
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
	public List<Operation> getOperationChildren(Operation actionParent, List<Operation> rootOperations) {
		final List<Operation> operations = new ArrayList<>();
		for (Operation operation : rootOperations) {
			if (operation.getNode().getParent().equals(actionParent)) {
				operations.add(operation);
			}
		}
		return operations;
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
		for (Operation operation : rootOperations) {
			if (operationWanted.isAssignableFrom(operation.getClass())) {
				return operation.getNode();
			}
		}
		throw new NoSuchElementException();
	}

	// Action.getName is private
	private String workAroundVisibility(String actionKind) {
		if ("INS".equals(actionKind)) {
			actionKind = "Insert";
		}
		if ("DEL".equals(actionKind)) {
			actionKind = "Delete";
		}
		if ("UPD".equals(actionKind)) {
			actionKind = "Update";
		}
		return actionKind;
	}

	@Override
	public boolean containsAction(String actionKind, String nodeKind) {
		actionKind = workAroundVisibility(actionKind);
		for (Operation operation : rootOperations) {
			if (operation.getAction().getClass().getSimpleName().equals(actionKind)) {
				if (context.getTypeLabel(operation.getAction().getNode()).equals(nodeKind)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsAction(String actionKind, String nodeKind, String nodeLabel) {
		return containsAction(getRootOperations(), actionKind, nodeKind, nodeLabel);
	}

	@Override
	public boolean containsAction(List<Operation> actions, String actionKind, String nodeKind, String nodeLabel) {
		actionKind = workAroundVisibility(actionKind);
		for (Operation operation : actions) {
			if (operation.getAction().getClass().getSimpleName().equals(actionKind)) {
				if (context.getTypeLabel(operation.getAction().getNode()).equals(nodeKind)) {
					if (operation.getAction().getNode().getLabel().equals(nodeLabel)) {
						return true;
					}
				}
			}
		}
		throw new AssertionError(actionKind + " " + nodeKind + " " + nodeLabel + "\n" + toDebugString());
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
			String nodeType = "CtfakenodeImpl";
			if (nodeElement != null) {
				nodeType = nodeElement.getClass().getSimpleName();
				nodeType = nodeType.substring(2, nodeType.length() - 4);
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
