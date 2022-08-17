package gumtree.spoon.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.TreeDelete;
import com.github.gumtreediff.actions.model.TreeInsert;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;

import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.DeleteTreeOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.InsertTreeOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.declaration.CtElement;

/**
 * @author Matias Martinez, matias.martinez@inria.fr
 */
public class DiffImpl implements Diff {
	/**
	 * Actions over all tree nodes (CtElements)
	 */
	private List<Operation> allOperations;
	/**
	 * Actions over the changes roots.
	 */
	private List<Operation> rootOperations;

	/**
	 * Actions over the changes roots.
	 */
	private List<Operation> simplifiedOperations;

	/**
	 * the mapping of this diff
	 */
	private MappingStore _mappingsComp;
	/**
	 * Context of the spoon diff.
	 */
	private TreeContext context;

	private GumtreeProperties properties = null;

	private Matcher matcher = new CompositeMatchers.ClassicGumtree();

	public DiffImpl(TreeContext context, Tree rootSpoonLeft, Tree rootSpoonRight, GumtreeProperties properties) {
		this.properties = properties;
		computeDiff(context, rootSpoonLeft, rootSpoonRight);
	}

	public DiffImpl(TreeContext context, Tree rootSpoonLeft, Tree rootSpoonRight, DiffConfiguration configuration) {
		this.matcher = configuration.getMatcher();
		computeDiff(context, rootSpoonLeft, rootSpoonRight);
	}

	public DiffImpl(TreeContext context, Tree rootSpoonLeft, Tree rootSpoonRight) {
		computeDiff(context, rootSpoonLeft, rootSpoonRight);
	}

	public void computeDiff(TreeContext context, Tree rootSpoonLeft, Tree rootSpoonRight) {
		if (context == null) {
			throw new IllegalArgumentException();
		}
		final MappingStore mappingsComp = new MappingStore(rootSpoonLeft, rootSpoonRight);
		this.context = context;

		if (properties != null)
			matcher.configure(properties);
		MappingStore mappings = matcher.match(rootSpoonLeft, rootSpoonRight, mappingsComp);

		// all actions

		EditScriptGenerator actionGenerator = new ChawatheScriptGenerator();

		EditScript edComplete = actionGenerator.computeActions(mappings);

		this.allOperations = convertToSpoon(edComplete.asList(), mappings);

		// Simplified

		EditScriptGenerator actionGeneratorSimplified = new SimplifiedChawatheScriptGenerator();

		EditScript edSimplified = actionGeneratorSimplified.computeActions(mappings);

		this.simplifiedOperations = convertToSpoon(edSimplified.asList(), mappings);

		// Roots

		ActionClassifier actionClassifier = new ActionClassifier(mappings, edComplete.asList());

		this.rootOperations = convertToSpoon(actionClassifier.getRootActions(), mappings);

		this._mappingsComp = mappingsComp;

		for (int i = 0; i < this.getAllOperations().size(); i++) {
			Operation operation = this.getAllOperations().get(i);
			if (operation instanceof MoveOperation) {
				if (operation.getSrcNode() != null) {
					operation.getSrcNode().putMetadata("isMoved", true);
				}
				if (operation.getDstNode() != null) {
					operation.getDstNode().putMetadata("isMoved", true);
				}
			}
		}
	}

	private List<Operation> convertToSpoon(List<Action> actions, MappingStore mappings) {
		List<Operation> collect = actions.stream().map(action -> {

			action.getNode().setMetadata("type", action.getNode().getType().name);
			final Tree original = action.getNode();

			if (action instanceof Insert) {
				return new InsertOperation((Insert) action);
			} else if (action instanceof Delete) {
				return new DeleteOperation((Delete) action);
			} else if (action instanceof Update) {
				setSpoonDestinationInTree(mappings, original);
				UpdateOperation updateOperation = new UpdateOperation((Update) action);
				return updateOperation;
			} else if (action instanceof Move) {
				setSpoonDestinationInTree(mappings, original);
				MoveOperation moveOperation = new MoveOperation((Move) action);
				return moveOperation;
			} else if (action instanceof TreeInsert) {
				// here? setSpoonDestinationInTree(mappings, original);
				return new InsertTreeOperation((TreeInsert) action);
			} else if (action instanceof TreeDelete) {
				return new DeleteTreeOperation((TreeDelete) action);
			} else {
				throw new IllegalArgumentException("Please support the new type " + action.getClass());
			}
		}).collect(Collectors.toList());
		return collect;
	}

	public void setSpoonDestinationInTree(MappingStore mappings, final Tree original) {
		Tree dest = mappings.getDstForSrc(original);
		original.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST, dest.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT));
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
				Tree parentTree = operation.getAction().getNode().getParent();
				Tree mappedSrcParent = _mappingsComp.getSrcForDst(parentTree);
				el = (CtElement) mappedSrcParent.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
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
						&& operation.getAction().getNode().getType().name.equals(nodeKind));
	}

	@Override
	public boolean containsOperation(OperationKind kind, String nodeKind, String nodeLabel) {
		return containsOperations(getRootOperations(), kind, nodeKind, nodeLabel);
	}

	@Override
	public boolean containsOperations(List<Operation> operations, OperationKind kind, String nodeKind,
			String nodeLabel) {
		return operations.stream()
				.anyMatch(operation -> operation.getAction().getClass().getSimpleName().equals(kind.name()) //
						&& operation.getAction().getNode().getType().name.equals(nodeKind)
						&& operation.getAction().getNode().getLabel().equals(nodeLabel));
	}

	@Override
	public boolean containsOperations(OperationKind kind, String nodeKind, String nodeLabel, String newLabel) {
		if (kind != OperationKind.Update) {
			throw new IllegalArgumentException();
		}
		return getRootOperations().stream().anyMatch(operation -> operation instanceof UpdateOperation //
				&& ((UpdateOperation) operation).getAction().getNode().getLabel().equals(nodeLabel)
				&& ((UpdateOperation) operation).getAction().getValue().equals(newLabel)

		);
	}

	@Override
	public void debugInformation() {
		System.err.println(toDebugString());
	}

	private String toDebugString() {
		return toDebugString(rootOperations);
	}

	private String toDebugString(List<Operation> ops) {
		String result = "";
		for (Operation operation : ops) {
			Tree node = operation.getAction().getNode();
			final CtElement nodeElement = operation.getNode();
			String nodeType = node.getType().name;
			if (nodeElement != null) {
				nodeType += "(" + nodeElement.getClass().getSimpleName() + ")";
			}
			result += "OperationKind." + operation.getAction().getClass().getSimpleName() + ", \"" + nodeType + "\", \""
					+ node.getLabel() + "\"";

			if (operation instanceof UpdateOperation) {
				// adding the new value for update
				result += ",  \"" + ((Update) operation.getAction()).getValue() + "\"";
			}

			result += " (size: " + node.getDescendants().size() + ")" + node.toTreeString();
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
			stringBuilder.append(operation.toString());

			// if all actions are applied on the same node print only the first action
			if (operation.getSrcNode().equals(ctElement) && operation instanceof UpdateOperation) {
				break;
			}
		}
		return stringBuilder.toString();
	}

	public TreeContext getContext() {
		return context;
	}

	public MappingStore getMappingsComp() {
		return _mappingsComp;
	}

	@Override
	public boolean containsOperations(List<Operation> operations, OperationKind kind, String nodeKind) {
		return operations.stream() //
				.anyMatch(operation -> operation.getAction().getClass().getSimpleName().equals(kind.name()) //
						&& operation.getAction().getNode().getType().name.equals(nodeKind));
	}
}
