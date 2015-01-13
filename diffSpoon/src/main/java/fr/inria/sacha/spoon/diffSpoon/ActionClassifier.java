package fr.inria.sacha.spoon.diffSpoon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.labri.gumtree.Mapping;
import fr.labri.gumtree.Mappings;
import fr.labri.gumtree.Tree;
import fr.labri.gumtree.actions.Action;
import fr.labri.gumtree.actions.Delete;
import fr.labri.gumtree.actions.Insert;
import fr.labri.gumtree.actions.Move;
import fr.labri.gumtree.actions.Permute;
import fr.labri.gumtree.actions.Update;

/**
 * Action Classifier
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class ActionClassifier {

	public boolean defaultOnlyRoots = true;

	Tree tl = null;
	Tree tr = null;
	public Set<Mapping> mappings = null;


	// /
	// ROOT CLASSIFIER
	// /
	protected Set<Tree> srcUpdTrees = new HashSet<Tree>();

	protected Set<Tree> dstUpdTrees = new HashSet<Tree>();

	protected Set<Tree> srcMvTrees = new HashSet<Tree>();

	protected Set<Tree> dstMvTrees = new HashSet<Tree>();

	protected Set<Tree> srcDelTrees = new HashSet<Tree>();

	protected Set<Tree> dstAddTrees = new HashSet<Tree>();

	protected Map<Tree, Action> originalActionsSrc = new HashMap<Tree, Action>();
	protected Map<Tree, Action> originalActionsDst = new HashMap<Tree, Action>();

	/**
	 * @return
	 * 
	 */
	public List<Action> getRootActions(Set<Mapping> rawMappings, List<Action> actions) {
		clean();
		Mappings mappings = new Mappings(rawMappings);

		for (Action a : actions) {

			if (a instanceof Delete) {
				srcDelTrees.add(a.getNode());
				originalActionsSrc.put(a.getNode(), a);
			} else if (a instanceof Insert) {
				dstAddTrees.add(a.getNode());
				originalActionsDst.put(a.getNode(), a);
			} else if (a instanceof Update) {
				srcUpdTrees.add(a.getNode());
				dstUpdTrees.add(mappings.getDst(a.getNode()));
				Tree dest = mappings.getDst(a.getNode());
				//dstMvTrees.add(dest);
				//originalActionsDst.put(dest, a);
				//New
				originalActionsSrc.put(a.getNode(), a);

			} else if (a instanceof Move || a instanceof Permute) {
				srcMvTrees.add(a.getNode());
				Tree dest = mappings.getDst(a.getNode());
				dstMvTrees.add(dest);
				//Bugfix? 
				//originalActionsDst.put(a.getNode(), a);
				originalActionsDst.put(dest, a);
			}
		}
		return retrieveRootActionsFromTreeNodes();
	}

	/**
	 * This method retrieves ONLY the ROOT actions
	 * 
	 * @return
	 */
	private List<Action> retrieveRootActionsFromTreeNodes() {

		List<Action> rootActions = new ArrayList<Action>();

/*		for (Tree t : dstUpdTrees) {
			// inc("UPD " + t.getTypeLabel() + " IN " +
			// t.getParent().getTypeLabel(), summary);
			Action a = originalActionsDst.get(t);
			rootActions.add(a);
		}*/
		
		//New: iterates source instead of dest
		for (Tree t : srcUpdTrees) {
			// inc("UPD " + t.getTypeLabel() + " IN " +
			// t.getParent().getTypeLabel(), summary);
			Action a = originalActionsSrc.get(t);
			rootActions.add(a);
		}
		for (Tree t : srcDelTrees) {
			if (!srcDelTrees.contains(t.getParent())) {
				Action a = originalActionsSrc.get(t);
				rootActions.add(a);

			}
		}
		for (Tree t : dstAddTrees) {
			if (!dstAddTrees.contains(t.getParent())) {
				Action a = originalActionsDst.get(t);
				rootActions.add(a);
			}
		}
		//Due to the change in getRootActions
	/*	for (Tree t : srcMvTrees) {
			if (!srcMvTrees.contains(t.getParent())) {
				Action a = originalActionsSrc.get(t);
				rootActions.add(a);
			}
		}*/
		for (Tree t : dstMvTrees) {
			if (!dstMvTrees.contains(t.getParent())) {
				Action a = originalActionsDst.get(t);
				rootActions.add(a);
			}
		}
		rootActions.removeAll(Collections.singleton(null));
		return rootActions;
	}


	
	
	private void clean() {
		srcUpdTrees.clear();

		dstUpdTrees.clear();

		srcMvTrees.clear();

		dstMvTrees.clear();

		srcDelTrees.clear();

		dstAddTrees.clear();

		originalActionsSrc.clear();

		originalActionsDst.clear();
	}
}
