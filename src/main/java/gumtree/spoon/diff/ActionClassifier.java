package gumtree.spoon.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;

import gumtree.spoon.builder.SpoonGumTreeBuilder;
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
	private List<ITree> srcUpdTrees = new ArrayList<>();
	private List<ITree> dstUpdTrees = new ArrayList<>();
	private List<ITree> srcMvTrees = new ArrayList<>();
	private List<ITree> dstMvTrees = new ArrayList<>();
	private List<ITree> srcDelTrees = new ArrayList<>();
	private List<ITree> dstAddTrees = new ArrayList<>();
	private Map<ITree, Action> originalActionsSrc = new HashMap<>();
	private Map<ITree, Action> originalActionsDst = new HashMap<>();

	public ActionClassifier(Set<Mapping> rawMappings, List<Action> actions) {
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
				original.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST,
						dest.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT));
				srcUpdTrees.add(original);
				dstUpdTrees.add(dest);
				originalActionsSrc.put(original, action);
			} else if (action instanceof Move) {
				ITree dest = mappings.getDst(original);
				original.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST,
						dest.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT));
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

	/**
	 * replaces moves by Insert/Delete operations
	 * 
	 * @param mapping
	 * @param ops
	 * @return
	 */
	public static List<Operation> replaceMove(MappingStore mapping, List<Operation> ops) {
		List<Operation> newOps = new ArrayList<>();

		for (Operation operation : ops) {

			if (operation instanceof MoveOperation) {

				MoveOperation movOp = (MoveOperation) operation;

				// Create the delete

				Delete deleteAction = new Delete(movOp.getAction().getNode());

				DeleteOperation delOp = new DeleteOperation(deleteAction);

				// add to the final list
				newOps.add(delOp);

				// Now the insert

				ITree node = mapping.getDst(movOp.getAction().getNode());
				ITree parent = (mapping.hasSrc(node)) ? mapping.getDst(node) : node;
				int pos = movOp.getPosition();

				Insert insertAc = new Insert(node, parent, pos);

				InsertOperation insertOp = new InsertOperation(insertAc);
				newOps.add(insertOp);

			} else {
				newOps.add(operation);
			}

		}

		return newOps;
	}

}
