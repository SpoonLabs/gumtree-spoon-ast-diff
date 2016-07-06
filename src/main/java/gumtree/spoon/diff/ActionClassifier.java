package gumtree.spoon.diff;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import gumtree.spoon.builder.SpoonGumTreeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Action Classifier
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 */
class ActionClassifier {
	// /
	// ROOT CLASSIFIER
	// /
	private Set<ITree> srcUpdTrees = new HashSet<>();
	private Set<ITree> dstUpdTrees = new HashSet<>();
	private Set<ITree> srcMvTrees = new HashSet<>();
	private Set<ITree> dstMvTrees = new HashSet<>();
	private Set<ITree> srcDelTrees = new HashSet<>();
	private Set<ITree> dstAddTrees = new HashSet<>();
	private Map<ITree, Action> originalActionsSrc = new HashMap<>();
	private Map<ITree, Action> originalActionsDst = new HashMap<>();

	List<Action> getRootActions(Set<Mapping> rawMappings, List<Action> actions) {
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
				originalActionsSrc.put(a.getNode(), a);
			} else if (a instanceof Move) {
				srcMvTrees.add(a.getNode());
				ITree dest = mappings.getDst(a.getNode());
				a.getNode().setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST, dest.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT));
				dstMvTrees.add(dest);
				originalActionsDst.put(dest, a);
			}
		}
		return retrieveRootActionsFromTreeNodes();
	}

	/**
	 * This method retrieves ONLY the ROOT actions
	 */
	private List<Action> retrieveRootActionsFromTreeNodes() {
		final List<Action> rootActions = new ArrayList<>();
		for (ITree t : srcUpdTrees) {
			rootActions.add(originalActionsSrc.get(t));
		}
		for (ITree t : srcDelTrees) {
			if (!srcDelTrees.contains(t.getParent()) && !srcUpdTrees.contains(t.getParent())) {
				rootActions.add(originalActionsSrc.get(t));
			}
		}
		for (ITree t : dstAddTrees) {
			if (!dstAddTrees.contains(t.getParent()) && !dstUpdTrees.contains(t.getParent())) {
				rootActions.add(originalActionsDst.get(t));
			}
		}
		for (ITree t : dstMvTrees) {
			if (!dstMvTrees.contains(t.getParent())) {
				rootActions.add(originalActionsDst.get(t));
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
