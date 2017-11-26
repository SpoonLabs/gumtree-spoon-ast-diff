package gumtree.spoon.diff;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test Spoon Diff
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class DiffTest {
	private final String newline = System.getProperty("line.separator");

	@Test
	public void testToString() throws Exception {
		File fl = new File("src/test/resources/examples/test1/TypeHandler1.java");
		File fr = new File("src/test/resources/examples/test1/TypeHandler2.java");

		AstComparator diff = new AstComparator();
		Diff result = diff.compare(fl,fr);
		assertEquals("Update FieldRead at org.apache.commons.cli.TypeHandler:80" +newline
				+ "\t(org.apache.commons.cli.PatternOptionBuilder.DATE_VALUE) to (org.apache.commons.cli.PatternOptionBuilder.CLASS_VALUE)" + newline
				+ "Insert Invocation at org.apache.commons.cli.TypeHandler:118" + newline
				+ "\tjava.lang.System.out.println(\"Hola\")" + newline, result.toString());

		fl = new File("src/test/resources/examples/test2/CommandLine1.java");
		fr = new File("src/test/resources/examples/test2/CommandLine2.java");

		result = diff.compare(fl,fr);
		assertEquals("Update Literal at org.apache.commons.cli.CommandLine:275" + newline
				+ "\t1 to 1000000" + newline, result.toString());

		fl = new File("src/test/resources/examples/test3/CommandLine1.java");
		fr = new File("src/test/resources/examples/test3/CommandLine2.java");

		result = diff.compare(fl,fr);
		String deleteStr = "Delete Method at org.apache.commons.cli.CommandLine:161" + newline
				+ "\tpublic java.lang.String[] getOptionValues(java.lang.String opt) {" + newline
				+ "\t    java.util.List<java.lang.String> values = new java.util.ArrayList<java.lang.String>();" + newline
				+ "\t    for (org.apache.commons.cli.Option option : options) {" + newline
				+ "\t        if ((opt.equals(option.getOpt())) || (opt.equals(option.getLongOpt()))) {" + newline
				+ "\t            values.addAll(option.getValuesList());" + newline
				+ "\t        }" + newline
				+ "\t    }" + newline
				+ "\t    return values.isEmpty() ? null : values.toArray(new java.lang.String[values.size()]);" + newline
				+ "\t}" + newline;
		assertTrue(result.toString(), result.toString().endsWith(deleteStr));
	}

	/**
	 * Add new element + child
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_1() throws Exception{
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");
		Diff result = diff.compare(fl,fr);

		List<Operation> actionsRoot = result.getRootOperations();
		//result.debugInformation();
		assertEquals(1, actionsRoot.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "If"));
	}

	/**
	 * changes an element and adds a new child to it
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_2() throws Exception{
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/roots/test9/left_QuickNotepad_1.13.java src/test/resources/examples/roots/test9/right_QuickNotepad_1.14.java
		File fl = new File("src/test/resources/examples/roots/test9/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test9/right_QuickNotepad_1.14.java");
		Diff result = diff.compare(fl,fr);

		List<Operation> actionsRoot = result.getRootOperations();
		//result.debugInformation();
		assertEquals(2, actionsRoot.size());
		assertEquals("(testArea) != null", actionsRoot.get(0).getSrcNode().toString());
		assertEquals("(testArea) == null", actionsRoot.get(0).getDstNode().toString());
		assertTrue(result.containsOperation(OperationKind.Update, "BinaryOperator"));
		assertTrue(result.containsOperation(OperationKind.Insert, "Return"));
		assertEquals(null, actionsRoot.get(1).getDstNode());
	}

	/**
	 * Remove element, keep its only child
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_3() throws Exception{
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test10/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test10/right_QuickNotepad_1.14.java");
		Diff result = diff.compare(fl,fr);

		List<Operation> actionsRoot = result.getRootOperations();
		//result.debugInformation();
		assertEquals(2, actionsRoot.size());

		assertTrue(result.containsOperation(OperationKind.Delete, "If"));
		assertTrue(result.containsOperation(OperationKind.Move, "Invocation"));
	}

	/**
	 * Removes element with 2 children, keep one child, remove the other
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_4() throws Exception{
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test11/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test11/right_QuickNotepad_1.14.java");
		Diff result = diff.compare(fl,fr);

		List<Operation> actionsRoot = result.getRootOperations();
		//result.debugInformation();
		assertEquals(2, actionsRoot.size());

		assertTrue(result.containsOperation(OperationKind.Delete, "If"));
		assertTrue(result.containsOperation(OperationKind.Move, "Invocation"));
	}

	/**
	 * Add element with 2 children, one new, other from a move
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_5() throws Exception{
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test12/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test12/right_QuickNotepad_1.14.java");
		Diff result = diff.compare(fl,fr);

		List<Operation> actionsRoot = result.getRootOperations();
		//result.debugInformation();
		assertEquals(2, actionsRoot.size());

		assertTrue(result.containsOperation(OperationKind.Insert, "If"));
		assertTrue(result.containsOperation(OperationKind.Move, "Invocation"));
	}
}
