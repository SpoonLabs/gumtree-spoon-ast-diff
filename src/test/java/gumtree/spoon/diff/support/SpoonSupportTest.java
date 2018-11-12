package gumtree.spoon.diff.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.gumtreediff.tree.ITree;

import gumtree.spoon.AstComparator;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.reflect.declaration.CtMethod;

public class SpoonSupportTest {

	@Test
	public void testRemove() {
		String c1 = "" + "class X {" + "public void foo0() {" + " int x = 0;return;" + "}" + "};";

		String c2 = "" + "class X {" + "public void foo0() {" + " int x = 0;" + "}" + "};";

		AstComparator diff = new AstComparator();
		Diff editScript = diff.compare(c1, c2);
		assertEquals(1, editScript.getRootOperations().size());

		Operation op = editScript.getRootOperations().get(0);
		assertTrue(op instanceof DeleteOperation);

		assertNotNull(op.getSrcNode());
		assertEquals("return", op.getSrcNode().toString());

		CtMethod methodSrc = op.getSrcNode().getParent(CtMethod.class);
		assertEquals(2, methodSrc.getBody().getStatements().size());
		assertNotNull(methodSrc);

		SpoonSupport support = new SpoonSupport();
		CtMethod methodTgt = (CtMethod) support.getMappedElement(editScript, methodSrc, true);
		assertNotNull(methodTgt);
		assertEquals("foo0", methodTgt.getSimpleName());
		assertEquals(1, methodTgt.getBody().getStatements().size());
		assertEquals("int x = 0", methodTgt.getBody().getStatements().get(0).toString());
	}

	@Test
	public void testUpdate() {
		String c1 = "" + "class X {" + "public int foo0() {" + " int x = 0;return 1;" + "}" + "};";

		String c2 = "" + "class X {" + "public int foo0() {" + " int x = 0;return 10;" + "}" + "};";

		AstComparator diff = new AstComparator();
		Diff editScript = diff.compare(c1, c2);
		assertEquals(1, editScript.getRootOperations().size());

		Operation op = editScript.getRootOperations().get(0);
		assertTrue(op instanceof UpdateOperation);

		assertNotNull(op.getSrcNode());
		assertEquals("1", op.getSrcNode().toString());

		CtMethod methodSrc = op.getSrcNode().getParent(CtMethod.class);
		assertNotNull(methodSrc);
		assertEquals(2, methodSrc.getBody().getStatements().size());
		assertEquals("return 1", methodSrc.getBody().getStatements().get(1).toString());

		SpoonSupport support = new SpoonSupport();
		CtMethod methodTgt = (CtMethod) support.getMappedElement(editScript, methodSrc, true);

		assertNotNull(methodTgt);
		assertEquals("foo0", methodTgt.getSimpleName());
		assertEquals(2, methodTgt.getBody().getStatements().size());
		assertEquals("return 10", methodTgt.getBody().getStatements().get(1).toString());
	}

	@Test
	public void testInsert1() {
		String c1 = "" + "class X {" + "public int foo0() {" + " int x = 0;return 1;" + "}" + "};";

		String c2 = "" + "class X {" + "public int foo0() {" + " int x = 0;x = 10;return 1;" + "}" + "};";

		AstComparator diff = new AstComparator();
		Diff editScript = diff.compare(c1, c2);
		assertEquals(1, editScript.getRootOperations().size());

		Operation op = editScript.getRootOperations().get(0);
		assertTrue(op instanceof InsertOperation);

		assertNotNull(op.getSrcNode());
		assertEquals("x = 10", op.getSrcNode().toString());

		CtMethod methodRight = op.getSrcNode().getParent(CtMethod.class);
		assertNotNull(methodRight);
		assertEquals("foo0", methodRight.getSimpleName());
		assertEquals(3, methodRight.getBody().getStatements().size());
		assertEquals("x = 10", methodRight.getBody().getStatements().get(1).toString());

		SpoonSupport support = new SpoonSupport();
		CtMethod methodLeft = (CtMethod) support.getMappedElement(editScript, methodRight, false);

		assertNotNull(methodLeft);
		assertEquals("foo0", methodLeft.getSimpleName());
		assertEquals(2, methodLeft.getBody().getStatements().size());
	}

	@Test
	public void testInsert1_SearchInWrong() {
		String c1 = "" + "class X {" + "public int foo0() {" + " int x = 0;return 1;" + "}" + "};";

		String c2 = "" + "class X {" + "public int foo0() {" + " int x = 0;x = 10;return 1;" + "}" + "};";

		AstComparator diff = new AstComparator();
		Diff editScript = diff.compare(c1, c2);
		assertEquals(1, editScript.getRootOperations().size());

		Operation op = editScript.getRootOperations().get(0);
		assertTrue(op instanceof InsertOperation);

		assertNotNull(op.getSrcNode());
		assertEquals("x = 10", op.getSrcNode().toString());

		CtMethod methodRight = op.getSrcNode().getParent(CtMethod.class);
		assertNotNull(methodRight);
		assertEquals("foo0", methodRight.getSimpleName());
		assertEquals(3, methodRight.getBody().getStatements().size());
		assertEquals("x = 10", methodRight.getBody().getStatements().get(1).toString());

		SpoonSupport support = new SpoonSupport();
		// Search one insert in left part
		CtMethod methodLeft = (CtMethod) support.getMappedElement(editScript, methodRight, true);

		assertNull(methodLeft);
	}

	@Test
	public void testGUMTREE_NODE_Link_1() {
		String c1 = "" + "class X {" + "public int foo0() {" + " int x = 0;return 1;" + "}" + "};";

		String c2 = "" + "class X {" + "public int foo0() {" + " int x = 0;x = 10;return 1;" + "}" + "};";

		AstComparator diff = new AstComparator();
		Diff editScript = diff.compare(c1, c2);
		assertEquals(1, editScript.getRootOperations().size());

		Operation op = editScript.getRootOperations().get(0);
		assertTrue(op instanceof InsertOperation);

		assertNotNull(op.getSrcNode());
		assertEquals("x = 10", op.getSrcNode().toString());

		ITree tree = (ITree) op.getSrcNode().getMetadata(SpoonGumTreeBuilder.GUMTREE_NODE);
		assertNotNull(tree);

		assertEquals("=", tree.getLabel());

		assertEquals(2, tree.getChildren().size());

	}

}
