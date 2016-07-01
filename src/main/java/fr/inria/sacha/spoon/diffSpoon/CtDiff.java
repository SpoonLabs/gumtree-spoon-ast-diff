package fr.inria.sacha.spoon.diffSpoon;

import com.github.gumtreediff.actions.model.Action;
import spoon.reflect.declaration.CtElement;

import java.util.List;

/** represents a diff between two Spoon ASTs */
public interface CtDiff {

	/** lists all actions (move,insert, deletes) */
	List<Action> getAllActions();

	/** lists all actions such that the parent is not involved in the diff */
	List<Action> getRootActions();

	/** returns the changed node if there is a single one */
	CtElement changedNode();

	/** returns the first changed node of a given gumtree Action class */
	CtElement changedNode(Class<? extends Action> class1);

	/** returns the common ancestor of all changes */
	CtElement commonAncestor();

	/** returns true if the diff contains a certain action */
	boolean containsAction(String actionKind, String nodeKind);

	/** returns true if the diff contains a certain action wheer the initial node is labeled with nodeLabel */
	boolean containsAction(String actionKind, String nodeKind, String nodeLabel);

	/** low level if you want to test on all actions and not only root actions */
	boolean containsAction(List<Action> actions, String actionKind, String nodeKind, String nodeLabel);

	/** outputs debug information to System.out */
	void debugInformation();

}