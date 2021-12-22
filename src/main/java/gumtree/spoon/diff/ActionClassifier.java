package gumtree.spoon.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.Tree;

import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;

/**
 * Action Classifier
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 */

public class ActionClassifier {
	// /
	// ROOT CLASSIFIER
	// /
	private List<Tree> srcUpdTrees = new ArrayList<>();
	private List<Tree> dstUpdTrees = new ArrayList<>();
	private List<Tree> srcMvTrees = new ArrayList<>();
	private List<Tree> dstMvTrees = new ArrayList<>();
	private List<Tree> srcDelTrees = new ArrayList<>();
	private List<Tree> dstAddTrees = new ArrayList<>();
	private Map<Tree, Action> originalActionsSrc = new HashMap<>();
	private Map<Tree, Action> originalActionsDst = new HashMap<>();

	public ActionClassifier(MappingStore mappings, List<Action> actions) {
		clean();

		for (Action action : actions) {
			final Tree original = action.getNode();
			if (action instanceof Delete) {
				srcDelTrees.add(original);
				originalActionsSrc.put(original, action);
			} else if (action instanceof Insert) {
				dstAddTrees.add(original);
				originalActionsDst.put(original, action);
			} else if (action instanceof Update) {
				Tree dest = mappings.getDstForSrc(original);
				srcUpdTrees.add(original);
				dstUpdTrees.add(dest);
				originalActionsSrc.put(original, action);
			} else if (action instanceof Move) {
				Tree dest = mappings.getDstForSrc(original);
				srcMvTrees.add(original);
				dstMvTrees.add(dest);
				originalActionsDst.put(dest, action);
			}
		}
	}

	/**
	 * This method retrieves ONLY the ROOT actions
	 */
	public List<Action> getRootActions() {
		final List<Action> rootActions = srcUpdTrees.stream().map(t -> originalActionsSrc.get(t))
				.collect(Collectors.toList());

		rootActions.addAll(srcDelTrees.stream() //
				.filter(t -> !srcDelTrees.contains(t.getParent()) && !srcUpdTrees.contains(t.getParent())) //
				.map(t -> originalActionsSrc.get(t)) //
				.collect(Collectors.toList()));

		rootActions.addAll(dstAddTrees.stream() //
				.filter(t -> !dstAddTrees.contains(t.getParent()) && !dstUpdTrees.contains(t.getParent())) //
				.map(t -> originalActionsDst.get(t)) //
				.collect(Collectors.toList()));

		rootActions.addAll(dstMvTrees.stream() //
				.filter(t -> !dstMvTrees.contains(t.getParent())) //
				.map(t -> originalActionsDst.get(t)) //
				.collect(Collectors.toList()));

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

	public static List<Operation> replaceMoveFromAll(Diff editScript) {
		return replaceMove(editScript.getMappingsComp(), editScript.getAllOperations(), true);
	}

	public static List<Operation> replaceMoveFromRoots(Diff editScript) {
		return replaceMove(editScript.getMappingsComp(), editScript.getRootOperations(), false);
	}

	/**
	 * replaces moves by Insert/Delete operations
	 * 
	 * @param mapping
	 * @param ops
	 * @return
	 */
	public static List<Operation> replaceMove(MappingStore mapping, List<Operation> ops, boolean all) {
		List<Operation> newOps = new ArrayList<>();

		List<Tree> dels = ops.stream().filter(e -> e instanceof DeleteOperation).map(e -> e.getAction().getNode())
				.collect(Collectors.toList());
		List<Tree> inss = ops.stream().filter(e -> e instanceof InsertOperation).map(e -> e.getAction().getNode())
				.collect(Collectors.toList());

		for (Operation operation : ops) {

			if (operation instanceof MoveOperation) {

				MoveOperation movOp = (MoveOperation) operation;
				Tree node = movOp.getAction().getNode();

				// Create the delete

				Delete deleteAction = new Delete(node);

				DeleteOperation delOp = new DeleteOperation(deleteAction);

				// add to the final list
				if (all || !inParent(dels, node.getParent())) {
					newOps.add(delOp);
				}

				// Now the insert
				Tree dstNode = mapping.getDstForSrc(node);
				Tree parentInAction = movOp.getAction().getParent();
				Tree parent = (mapping.isDstMapped(parentInAction)) ? mapping.getDstForSrc(parentInAction)
						: parentInAction;
				int pos = movOp.getPosition();

				Insert insertAc = new Insert(dstNode, parent, pos);

				InsertOperation insertOp = new InsertOperation(insertAc);

				if (all || !inParent(inss, parentInAction)) {
					newOps.add(insertOp);
				}

			} else {
				newOps.add(operation);
			}

		}

		return newOps;
	}

	public static boolean inParent(List<Tree> trees, Tree parent) {
		if (parent == null) {
			return false;
		}
		if (trees.contains(parent))
			return true;
		else
			return inParent(trees, parent.getParent());

	}
}
