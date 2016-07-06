package gumtree.spoon.diff;

import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;

import java.util.List;

/**
 * represents a diff between two Spoon ASTs
 */
public interface Diff {

	/**
	 * lists all actions (move,insert, deletes)
	 */
	List<Operation> getAllOperations();

	/**
	 * lists all actions such that the parent is not involved in the diff
	 */
	List<Operation> getRootOperations();

	/**
	 * lists all actions sub the given parent action.
	 */
	List<Operation> getOperationChildren(Operation actionParent, List<Operation> rootActions);

	/**
	 * returns the changed node if there is a single one
	 */
	CtElement changedNode();

	/**
	 * returns the first changed node of a given gumtree Action class
	 */
	CtElement changedNode(Class<? extends Operation> class1);

	/**
	 * returns the common ancestor of all changes
	 */
	CtElement commonAncestor();

	/**
	 * returns true if the diff contains a certain action
	 */
	boolean containsAction(String actionKind, String nodeKind);

	/**
	 * returns true if the diff contains a certain action wheer the initial node is labeled with nodeLabel
	 */
	boolean containsAction(String actionKind, String nodeKind, String nodeLabel);

	/**
	 * low level if you want to test on all actions and not only root actions
	 */
	boolean containsAction(List<Operation> actions, String actionKind, String nodeKind, String nodeLabel);

	/**
	 * outputs debug information to System.out
	 */
	void debugInformation();

}
