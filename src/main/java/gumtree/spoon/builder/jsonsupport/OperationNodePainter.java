package gumtree.spoon.builder.jsonsupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import com.google.gson.JsonObject;

import gumtree.spoon.builder.Json4SpoonGenerator.JSON_PROPERTIES;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez
 *
 */
public class OperationNodePainter implements NodePainter {

	private Map<Tree, Operation> nodesAffectedByOps = new HashMap<>();

	public OperationNodePainter(List<Operation> operations) {
		// Collect all nodes and get the operator
		for (Operation operation : operations) {
			nodesAffectedByOps.put(operation.getAction().getNode(), operation);
		}
	}

	@Override
	public void paint(Tree tree, JsonObject jsontree) {

		if (nodesAffectedByOps.containsKey(tree)) {

			Operation<Action> operationOverTree = nodesAffectedByOps.get(tree);
			jsontree.addProperty(JSON_PROPERTIES.op.toString(), operationOverTree.getAction().getName());
		}
	}

}
