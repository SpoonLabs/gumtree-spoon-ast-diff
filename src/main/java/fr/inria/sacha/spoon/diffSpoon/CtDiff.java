package fr.inria.sacha.spoon.diffSpoon;

import java.util.List;

import spoon.reflect.declaration.CtElement;

import com.github.gumtreediff.actions.model.Action;

/** represents a diff between two Spoon ASTs */
public interface CtDiff {

	/** lists all actions (move,insert, deletes) */
	public List<Action> getAllActions();

	/** lists all actions such that the parent is not involved in the diff */
	public List<Action> getRootActions();

	/** returns the changed node if there is a single one */
	public CtElement changedNode();

	/** returns the first changed node of a given gumtree Action class */
	public CtElement changedNode(Class<? extends Action>class1);


	/** returns the common ancestor of all changes */
	public CtElement commonAncestor();

	/** returns true if the diff contains a certain action */
	public boolean containsAction(String actionKind, String nodeKind);

	/** returns true if the diff contains a certain action wheer the initial node is labeled with nodeLabel */
	public boolean containsAction(String actionKind, String nodeKind, String nodeLabel);

	/** outputs debug information to System.out */
	public void debugInformation();

}