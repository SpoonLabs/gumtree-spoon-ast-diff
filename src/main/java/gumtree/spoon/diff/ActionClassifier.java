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
		for (Action action : actions) {
			final ITree original = action.getNode();
			if (action instanceof Delete) {
				srcDelTrees.add(original);
				originalActionsSrc.put(original, action);
			} else if (action instanceof Insert) {
				dstAddTrees.add(original);
				originalActionsDst.put(original, action);
			} else if (action instanceof Update) {
				ITree dest = mappings.getDst(original);
				original.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST, dest.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT));
				srcUpdTrees.add(original);
				dstUpdTrees.add(dest);
				originalActionsSrc.put(original, action);
			} else if (action instanceof Move) {
				ITree dest = mappings.getDst(original);
				original.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST, dest.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT));
				srcMvTrees.add(original);
				dstMvTrees.add(dest);
				originalActionsDst.put(dest, action);
			}
		}
		return getRootActions();
	}

	/**
	 * This method retrieves ONLY the ROOT actions
	 */
	private List<Action> getRootActions() {
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
