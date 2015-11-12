package fr.inria.sacha.spoon.diffSpoon;

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.declaration.CtElement;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.matchers.MappingStore;




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

	/** the mapping of this diff */
	protected MappingStore _mappingsComp = null;

	
	public CtDiff(List<Action> allActions, List<Action> rootActions, MappingStore mappingsComp) {
		super();
		this.allActions = allActions;
		this.rootActions = rootActions;
		this._mappingsComp = mappingsComp;
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
	
	/** returns the common ancestor of all changes */
	public CtElement commonAncestor() {
		List<CtElement> copy = new ArrayList<>();
		for(Action a: rootActions) {
			CtElement el=null;
			if (a instanceof Insert) {
				// we take the corresponding node in the source tree
				el = (CtElement) _mappingsComp.getSrc(a.getNode().getParent()).getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			} else {
				el = (CtElement) a.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			}
			copy.add(el);
		}
		while (copy.size()>=2) {
			CtElement first = copy.remove(0);
			CtElement second = copy.remove(0);
			copy.add(commonAncestor(first, second,0));
		}
		return copy.get(0);
	}

	private CtElement commonAncestor(CtElement first, CtElement second, int i) {
		while (first!=null) {
			CtElement el = second;
			while (el!=null) {
				if (first == el) {
					return first;
				}
				el = el.getParent();
			}
			first = first.getParent();
		}
		return null;		
	}

}
