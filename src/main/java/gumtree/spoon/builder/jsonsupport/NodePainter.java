package gumtree.spoon.builder.jsonsupport;

import com.github.gumtreediff.tree.Tree;
import com.google.gson.JsonObject;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface NodePainter {

	public void paint(Tree tree, JsonObject jsontree);
}
