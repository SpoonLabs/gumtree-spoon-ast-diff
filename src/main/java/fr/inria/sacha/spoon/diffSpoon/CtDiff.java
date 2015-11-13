package fr.inria.sacha.spoon.diffSpoon;

import java.util.ArrayList;
import java.util.List;

import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

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
	
	/** returns the changed node if there is a single one */
	public CtElement changedNode() {
		if (rootActions.size()!=1) {
			throw new IllegalArgumentException();
		}
		return commonAncestor();
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

	@Override
	public String toString(){
		String newline = System.getProperty("line.separator");
		StringBuilder stringBuilder = new StringBuilder();
		CtElement ctElement = commonAncestor();
		for (Action action : getRootActions()) {
			CtElement element = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			// action name
			stringBuilder.append(action.getClass().getSimpleName());

			// node type
			String nodeType = element.getClass().getSimpleName();
			nodeType = nodeType.substring(2, nodeType.length() - 4);
			stringBuilder.append(" " + nodeType);

			// action position
			CtElement parent = element;
			while (parent.getParent() != null && !(parent.getParent() instanceof CtPackage)) {
				parent = parent.getParent();
			}
			String position = " at ";
			if(parent instanceof CtType) {
				position += ((CtType)parent).getQualifiedName();
			}
			position += ":" + element.getPosition().getLine();
			if(action instanceof Move) {
				CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
				position = " from " + element.getParent(CtClass.class).getQualifiedName()
						+ ":" + element.getPosition().getLine();
				position += " to " + elementDest.getParent(CtClass.class).getQualifiedName()
						+ ":" + elementDest.getPosition().getLine();
			}
			stringBuilder.append(position + newline);

			// code change
			String label = element.toString();
			if (action instanceof Update) {
				CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
				label += " to " + elementDest.toString();
			}
			String[] split = label.split(newline);
			for (int i = 0; i < split.length; i++) {
				String s = split[i];
				stringBuilder.append("\t" + s + newline);
			}

			// if all actions are applied on the same node print only the first action
			if(element.equals(ctElement) && action instanceof Update) {
				break;
			}
		}
		return stringBuilder.toString();
	}

}
