package gumtree.spoon.diff;

import java.util.List;

import com.github.gumtreediff.matchers.MappingStore;

import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import spoon.reflect.declaration.CtElement;

/**
 * represents a diff between two Spoon ASTs
 */
public interface Diff {

	/**
	 * lists all operations such that the parent is not involved in the diff
	 */
	List<Operation> getRootOperations();

	/**
	 * lists all operations (move,insert, deletes). Low-level operation, we
	 * recommend using {@link #getRootOperations()}
	 */
	List<Operation> getAllOperations();

	/**
	 * lists all operations sub the given parent operation.
	 */
	List<Operation> getOperationChildren(Operation operationParent, List<Operation> rootOperations);

	/**
	 * returns the changed node if there is a single one
	 */
	CtElement changedNode();

	/**
	 * returns the first changed node of a given gumtree Operation class
	 */
	CtElement changedNode(Class<? extends Operation> operationWanted);

	/**
	 * returns the common ancestor of all changes
	 */
	CtElement commonAncestor();

	/**
	 * returns true if the diff contains a certain operation
	 */
	boolean containsOperation(OperationKind kind, String nodeKind);

	/**
	 * returns true if the diff contains a certain operation where the initial node
	 * is labeled with nodeLabel
	 */
	boolean containsOperation(OperationKind kind, String nodeKind, String nodeLabel);

	/**
	 * Useful for update operation
	 */
	boolean containsOperations(OperationKind kind, String nodeKind, String nodeLabel, String newLabel);

	/**
	 * low level if you want to test on all operations and not only root operations
	 */
	boolean containsOperations(List<Operation> operations, OperationKind kind, String nodeKind, String nodeLabel);

	/**
	 * low level if you want to test on all operations and not only root operations
	 */
	boolean containsOperations(List<Operation> operations, OperationKind kind, String nodeKind);

	/**
	 * outputs debug information to System.out
	 */
	void debugInformation();

	/**
	 * returns the mappings between the compared elements
	 */
	public MappingStore getMappingsComp();
}
