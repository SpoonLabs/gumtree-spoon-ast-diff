package fr.inria.sacha.spoon.diffSpoon;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;




/**
 * 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class CtDiffImpl implements CtDiff {
	/**
	 * Actions over all tree nodes (CtElements) 
	 */
	private List<Action> allActions = null;
	/**
	 * Actions over the changes roots.
	 */
	private List<Action> rootActions = null;

	/** the mapping of this diff */
	protected MappingStore _mappingsComp = null;

	private SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
	
	public CtDiffImpl(List<Action> allActions, List<Action> rootActions, MappingStore mappingsComp, SpoonGumTreeBuilder scanner) {
		super();
		this.allActions = allActions;
		this.rootActions = rootActions;
		this._mappingsComp = mappingsComp;
		this.scanner = scanner;
	}
	
	@Override
	public List<Action> getAllActions() {
		return allActions;
	}
	public void setAllActions(List<Action> allActions) {
		this.allActions = allActions;
	}
	@Override
	public List<Action> getRootActions() {
		return rootActions;
	}
	public void setRootActions(List<Action> rootActions) {
		this.rootActions = rootActions;
	}
	
	/** returns the changed node if there is a single one */
	@Override
	public CtElement changedNode() {
		if (rootActions.size()!=1) {
			throw new IllegalArgumentException();
		}
		return commonAncestor();
	}
	
	/** returns the common ancestor of all changes */
	@Override
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
		if (getRootActions().size()==0) {
			return "no AST change";
		}
		StringBuilder stringBuilder = new StringBuilder();
		CtElement ctElement = commonAncestor();
		for (Action action : getRootActions()) {
			stringBuilder.append(toStringAction(action));

			// if all actions are applied on the same node print only the first action
			CtElement element = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			if(element.equals(ctElement) && action instanceof Update) {
				break;
			}
		}
		return stringBuilder.toString();
	}

	public String toStringAction(Action action) {
		String newline = System.getProperty("line.separator");
		StringBuilder stringBuilder = new StringBuilder();

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
		if (element.getPosition()!=null) {
		  position += ":" + element.getPosition().getLine();
		}
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
		return stringBuilder.toString();
	}

	public void debugInformation(){
		System.err.println(toDebugString());
	}
	
	public String toDebugString() {
		String result = "";
		for (Action action : getRootActions()) {
			ITree node = action.getNode();
			String label = "\"" + node.getLabel() + "\"";
			if (action instanceof Update) {
				label+= " to \""+((Update)action).getValue()+"\"";
			}
			String nodeType = "CtfakenodeImpl";
			if (node.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT)!=null) {
				nodeType = node.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT).getClass().getSimpleName();
				nodeType = nodeType.substring(2, nodeType.length() - 4);
			}
			result +=
					"\"" + action.getClass().getSimpleName()+ "\"," 
					+ " " +"\"" + nodeType+ "\","
					+ " " +label					
					+ " (size: " +node.getDescendants().size()	+")"				
					+ node.toTreeString()
					;
		}	
		return result;
	}

	@Override
	public CtElement changedNode(
			Class class1) {
		for (Action a:getRootActions()) {
			if (class1.isAssignableFrom(a.getClass())) {
				return (CtElement) a.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
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
	public boolean containsAction(String actionKind,
			String nodeKind) {
		actionKind = workAroundVisibility(actionKind);
		for (Action action : getRootActions()) {
			if (action.getClass().getSimpleName().equals(actionKind)) {
				if (scanner.gtContext.getTypeLabel(action.getNode()).equals(
						nodeKind)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsAction(String actionKind,
			String nodeKind, String nodeLabel) {
		return containsAction(getRootActions(), actionKind, nodeKind, nodeLabel);
	}
	
	/** low level if you want to test on all actions and not only root actions */
	public boolean containsAction(List<Action> actions, String actionKind,
			String nodeKind, String nodeLabel) {
		actionKind = workAroundVisibility(actionKind);
		for (Action action : actions) {
			if (action.getClass().getSimpleName().equals(actionKind)) {
				if (scanner.gtContext.getTypeLabel(action.getNode()).equals(
						nodeKind)) {
					if (action.getNode().getLabel().equals(nodeLabel)) {
						return true;
					}
				}
			}
		}
		throw new AssertionError(actionKind + " " + nodeKind + " " + nodeLabel+"\n"+toDebugString());
	}

	
}
