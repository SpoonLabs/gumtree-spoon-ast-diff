package gumtree.spoon.builder;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import spoon.reflect.declaration.CtElement;

/**
 *
 * Creates a JSON representation of a Spoon Java abstract syntaxt tree.
 *
 * @author Matias Martinez
 *
 */
public class Json4SpoonGenerator {

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
		return gson.toJson(this.getJSONasJsonObject(tcontext, generatedTree))+"\n";
	}


	private JsonObject getJSONasJsonObject(TreeContext context, ITree tree) {
		JsonObject o = new JsonObject();
		o.addProperty("label", tree.getLabel());
		o.addProperty("type", context.getTypeLabel(tree));

		JsonArray nodeChildens = new JsonArray();
		o.add("children", nodeChildens);

		for (ITree tch : tree.getChildren()) {
			JsonObject childJSon = getJSONasJsonObject(context, tch);
			if (childJSon != null)
				nodeChildens.add(childJSon);
		}
		return o;

	}

}
