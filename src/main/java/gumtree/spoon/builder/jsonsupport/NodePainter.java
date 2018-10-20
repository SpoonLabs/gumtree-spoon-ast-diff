package gumtree.spoon.builder.jsonsupport;

import com.github.gumtreediff.tree.ITree;
import com.google.gson.JsonObject;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface NodePainter {

	public void paint(ITree tree, JsonObject jsontree);
}
