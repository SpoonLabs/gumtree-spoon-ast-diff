package gumtree.spoon.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;

/**
 *
 * Creates a JSON representation of a Spoon Java abstract syntaxt tree.
 *
 * @author Matias Martinez
 *
 */
public class Json4SpoonGenerator {

	public enum JSON_PROPERTIES {
		label, type, op, children;
	};

	@SuppressWarnings("rawtypes")
	public JsonObject getJSONasJsonObject(CtElement element) {
		SpoonGumTreeBuilder builder = new SpoonGumTreeBuilder();
		ITree generatedTree = builder.getTree(element);

		TreeContext tcontext = builder.getTreeContext();
		return this.getJSONasJsonObject(tcontext, generatedTree);
	}

	public String getJSONasString(CtElement element) {
		SpoonGumTreeBuilder builder = new SpoonGumTreeBuilder();
		ITree generatedTree = builder.getTree(element);

		TreeContext tcontext = builder.getTreeContext();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this.getJSONasJsonObject(tcontext, generatedTree)) + "\n";
	}

	private JsonObject getJSONasJsonObject(TreeContext context, ITree tree) {
		JsonObject o = new JsonObject();
		o.addProperty(JSON_PROPERTIES.label.toString(), tree.getLabel());
		o.addProperty(JSON_PROPERTIES.type.toString(), context.getTypeLabel(tree));

		JsonArray nodeChildens = new JsonArray();
		o.add(JSON_PROPERTIES.children.toString(), nodeChildens);

		for (ITree tch : tree.getChildren()) {
			JsonObject childJSon = getJSONasJsonObject(context, tch);
			if (childJSon != null)
				nodeChildens.add(childJSon);
		}
		return o;

	}

	/**
	 * Decorates a node with the affected operator, if any.
	 * 
	 * @param context
	 * @param tree
	 * @param operations
	 * @return
	 */
	public JsonObject getJSONasJsonObject(TreeContext context, ITree tree, List<Operation> operations) {

		// Collect all nodes and get the operator
		Map<ITree, Operation> nodesAffectedByOps = new HashMap<>();
		for (Operation operation : operations) {
			nodesAffectedByOps.put(operation.getAction().getNode(), operation);
		}
		return getJSONasJsonObject(context, tree, operations, nodesAffectedByOps);
	}

	@SuppressWarnings("unused")
	private JsonObject getJSONasJsonObject(TreeContext context, ITree tree, List<Operation> operations,
			Map<ITree, Operation> nodesAffectedByOps) {

		JsonObject o = new JsonObject();
		o.addProperty(JSON_PROPERTIES.label.toString(), tree.getLabel());
		o.addProperty(JSON_PROPERTIES.type.toString(), context.getTypeLabel(tree));
		if (nodesAffectedByOps.containsKey(tree)) {

			Operation<Action> operationOverTree = nodesAffectedByOps.get(tree);
			o.addProperty(JSON_PROPERTIES.op.toString(), operationOverTree.getAction().getName());
		}

		JsonArray nodeChildens = new JsonArray();
		o.add(JSON_PROPERTIES.children.toString(), nodeChildens);

		for (ITree tch : tree.getChildren()) {
			JsonObject childJSon = getJSONasJsonObject(context, tch, operations);
			if (childJSon != null)
				nodeChildens.add(childJSon);
		}
		return o;

	}

}
