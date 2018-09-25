package gumtree.spoon.builder;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import spoon.reflect.declaration.CtType;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TreeJSONOutput {

	@SuppressWarnings("rawtypes")
	public JsonObject getJSON(CtType type) {
		SpoonGumTreeBuilder builder = new SpoonGumTreeBuilder();
		ITree generatedTree = builder.getTree(type);

		TreeContext tcontext = builder.getTreeContext();
		return this.getJSON(tcontext, generatedTree);
	}

	public JsonObject getJSON(TreeContext context, ITree tree) {
		JsonObject o = new JsonObject();
		o.addProperty("label", tree.getLabel());
		o.addProperty("type", context.getTypeLabel(tree));

		JsonArray nodeChildens = new JsonArray();
		o.add("children", nodeChildens);

		for (ITree tch : tree.getChildren()) {
			JsonObject childJSon = getJSON(context, tch);
			if (childJSon != null)
				nodeChildens.add(childJSon);
		}
		return o;

	}

}
