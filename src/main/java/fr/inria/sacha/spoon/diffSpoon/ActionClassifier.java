package fr.inria.sacha.spoon.diffSpoon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
/**
 * Action Classifier
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class ActionClassifier {

	public boolean defaultOnlyRoots = true;

	ITree tl = null;
	ITree tr = null;
	public Set<Mapping> mappings = null;


	// /
	// ROOT CLASSIFIER
	// /
	protected Set<ITree> srcUpdTrees = new HashSet<>();

	protected Set<ITree> dstUpdTrees = new HashSet<>();

	protected Set<ITree> srcMvTrees = new HashSet<>();

	protected Set<ITree> dstMvTrees = new HashSet<>();

	protected Set<ITree> srcDelTrees = new HashSet<>();

	protected Set<ITree> dstAddTrees = new HashSet<>();

	protected Map<ITree, Action> originalActionsSrc = new HashMap<>();
	protected Map<ITree, Action> originalActionsDst = new HashMap<>();

	/**
	 * @return
	 * 
	 */
	public List<Action> getRootActions(Set<Mapping> rawMappings, List<Action> actions) {
		clean();
		MappingStore mappings = new MappingStore(rawMappings);

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
				ITree dest = mappings.getDst(a.getNode());
				a.getNode().setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST, dest.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT));
				//dstMvTrees.add(dest);
				//originalActionsDst.put(dest, a);
				//New
				originalActionsSrc.put(a.getNode(), a);

			} else if (a instanceof Move /* || a instanceof Permute*/) {
				srcMvTrees.add(a.getNode());
				ITree dest = mappings.getDst(a.getNode());
				a.getNode().setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST, dest.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT));
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
		for (ITree t : srcUpdTrees) {
			// inc("UPD " + t.getTypeLabel() + " IN " +
			// t.getParent().getTypeLabel(), summary);
			Action a = originalActionsSrc.get(t);
			rootActions.add(a);
		}
		for (ITree t : srcDelTrees) {
			if (!srcDelTrees.contains(t.getParent())) {
				Action a = originalActionsSrc.get(t);
				rootActions.add(a);

			}
		}
		for (ITree t : dstAddTrees) {
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
		for (ITree t : dstMvTrees) {
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
