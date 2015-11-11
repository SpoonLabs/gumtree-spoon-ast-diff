package fr.inria.sacha.spoon.diffSpoon;

import java.util.List;

import com.github.gumtreediff.actions.model.Action;




/**
 * 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class CtDiff {
	/**
	 * Actions over all tree nodes (CtElements) 
	 */
	List<Action> allActions = null;
	/**
	 * Actions over the changes roots.
	 */
	List<Action> rootActions = null;
	
	
	
	public CtDiff(List<Action> allActions, List<Action> rootActions) {
		super();
		this.allActions = allActions;
		this.rootActions = rootActions;
	}
	public List<Action> getAllActions() {
		return allActions;
	}
	public void setAllActions(List<Action> allActions) {
		this.allActions = allActions;
	}
	public List<Action> getRootActions() {
		return rootActions;
	}
	public void setRootActions(List<Action> rootActions) {
		this.rootActions = rootActions;
	}
	
	@Override
	public String toString() {
		return rootActions.toString();
	}
}
