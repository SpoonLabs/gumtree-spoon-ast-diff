package gumtree.spoon.diff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

/**
 * Test Spoon Diff
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class DiffTest {
	private final String newline = System.getProperty("line.separator");

	@Test
	public void testToString() throws Exception {
		File fl = new File("src/test/resources/examples/test1/TypeHandler1.java");
		File fr = new File("src/test/resources/examples/test1/TypeHandler2.java");

		AstComparator diff = new AstComparator();
		Diff result = diff.compare(fl, fr);
		// one previous versions, it was 2, which was better
		// but this one also makes sense
		// since two other tests are improved
		// test_t_225391 and testD4JLang57
		// we keep it
		assertEquals(4, result.getRootOperations().size());
		fl = new File("src/test/resources/examples/test2/CommandLine1.java");
		fr = new File("src/test/resources/examples/test2/CommandLine2.java");

		result = diff.compare(fl, fr);
		assertEquals("Update Literal at org.apache.commons.cli.CommandLine:275" + newline + "\t1 to 1000000" + newline,
				result.toString());

		fl = new File("src/test/resources/examples/test3/CommandLine1.java");
		fr = new File("src/test/resources/examples/test3/CommandLine2.java");

		result = diff.compare(fl, fr);
		String deleteStr = "Delete Method at org.apache.commons.cli.CommandLine:161" + newline
				+ "\tpublic java.lang.String[] getOptionValues(java.lang.String opt) {" + newline
				+ "\t    java.util.List<java.lang.String> values = new java.util.ArrayList<java.lang.String>();"
				+ newline + "\t    for (org.apache.commons.cli.Option option : options) {" + newline
				+ "\t        if (opt.equals(option.getOpt()) || opt.equals(option.getLongOpt())) {" + newline
				+ "\t            values.addAll(option.getValuesList());" + newline + "\t        }" + newline + "\t    }"
				+ newline
				+ "\t    return values.isEmpty() ? null : values.toArray(new java.lang.String[values.size()]);"
				+ newline + "\t}" + newline;
		assertTrue(result.toString(), result.toString().endsWith(deleteStr));
	}

	/**
	 * Add new element + child
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_1() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actionsRoot = result.getRootOperations();
		assertEquals(1, actionsRoot.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "If"));
	}

	/**
	 * changes an element and adds a new child to it
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_2() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/roots/test9/left_QuickNotepad_1.13.java
		// src/test/resources/examples/roots/test9/right_QuickNotepad_1.14.java
		File fl = new File("src/test/resources/examples/roots/test9/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test9/right_QuickNotepad_1.14.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actionsRoot = result.getRootOperations();
		assertEquals(2, actionsRoot.size());
		assertEquals("testArea != null", actionsRoot.get(0).getSrcNode().toString());
		assertEquals("testArea == null", actionsRoot.get(0).getDstNode().toString());
		assertTrue(result.containsOperation(OperationKind.Update, "BinaryOperator"));
		assertTrue(result.containsOperation(OperationKind.Insert, "Return"));
		assertEquals(null, actionsRoot.get(1).getDstNode());
	}

	/**
	 * Remove element, keep its only child
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_3() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test10/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test10/right_QuickNotepad_1.14.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actionsRoot = result.getRootOperations();
		assertEquals(2, actionsRoot.size());

		assertTrue(result.containsOperation(OperationKind.Delete, "If"));
		assertTrue(result.containsOperation(OperationKind.Move, "Invocation"));
	}

	/**
	 * Removes element with 2 children, keep one child, remove the other
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_4() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test11/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test11/right_QuickNotepad_1.14.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actionsRoot = result.getRootOperations();
		assertEquals(2, actionsRoot.size());

		assertTrue(result.containsOperation(OperationKind.Delete, "If"));
		assertTrue(result.containsOperation(OperationKind.Move, "Invocation"));
	}

	/**
	 * Add element with 2 children, one new, other from a move
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_5() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test12/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test12/right_QuickNotepad_1.14.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actionsRoot = result.getRootOperations();
		assertEquals(2, actionsRoot.size());

		assertTrue(result.containsOperation(OperationKind.Insert, "If"));
		assertTrue(result.containsOperation(OperationKind.Move, "Invocation"));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void test_action_RootOrder() throws Exception {
		AstComparator diff = new AstComparator();

		CtClass c1b = Launcher.parseClass(" class BehaviorCall implements Call{\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "\n" + " enqueue(new Callback<T>() {\n"
				+ "  @Override public void onResponse(Response<T> response) {\n" + "     responseRef.set(response);\n"
				+ "     latch.countDown();\n" + "   }\n" + "}\n" + ")\n" + "\n" + "}");

		CtClass c2b = Launcher.parseClass("class BehaviorCall implements Call {\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "\n" + " enqueue(new Callback<T>() {\n"
				+ "@Override public void onResponse(Response<T> response) {\n"

				// Added
				+ "System.out.println();"
				//
				+ "responseRef.set(response);\n"
				// Diff
				+ "if(response != null){return response;}" + "latch.countDown();\n" + "}\n" + "}\n" + ")\n" + "}");

		Diff result = diff.compare(c1b, c2b);

		List<Operation> actionsRoot = result.getRootOperations();
		// Buggy
		actionsRoot = result.getRootOperations();
		assertEquals(2, actionsRoot.size());

		Operation oproot0 = result.getRootOperations().get(0);
		Operation oproot1 = result.getRootOperations().get(1);

		int indexInAll0 = result.getAllOperations().indexOf(oproot0);
		int indexInAll1 = result.getAllOperations().indexOf(oproot1);

		// Preserve the position
		assertTrue(indexInAll0 < indexInAll1);

	}

	@Test
	public void test_diffOfGenericTypeReference_builtInTypeToBuiltInType() throws Exception {
		File left = new File("src/test/resources/examples/diffOfGenericTypeReferences/builtInTypeToBuiltInType/left.java");
		File right = new File("src/test/resources/examples/diffOfGenericTypeReferences/builtInTypeToBuiltInType/right.java");

		Diff diff = new AstComparator().compare(left, right);
		assertEquals(2, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Delete, "TypeReference"));
		assertTrue(diff.containsOperation(OperationKind.Insert, "WildcardReference"));
	}

	@Test
	public void test_diffOfGenericTypeReference_builtInTypeToTypeParameter() throws Exception {
		File left = new File("src/test/resources/examples/diffOfGenericTypeReferences/builtInTypeToTypeParameter/left.java");
		File right = new File("src/test/resources/examples/diffOfGenericTypeReferences/builtInTypeToTypeParameter/right.java");

		Diff diff = new AstComparator().compare(left, right);
		assertEquals(2, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Delete, "TypeReference"));
		assertTrue(diff.containsOperation(OperationKind.Insert, "TypeParameterReference"));
	}

	@Test
	public void test_diffOfGenericTypeReference_typeParameterToBuiltInType() throws Exception {
		File left = new File("src/test/resources/examples/diffOfGenericTypeReferences/typeParameterToBuiltInType/left.java");
		File right = new File("src/test/resources/examples/diffOfGenericTypeReferences/typeParameterToBuiltInType/right.java");

		Diff diff = new AstComparator().compare(left, right);
		assertEquals(2, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Insert, "TypeReference"));
		assertTrue(diff.containsOperation(OperationKind.Delete, "TypeParameterReference"));
	}

	@Test
	public void test_diffOfAnnotations() throws Exception {
		File left = new File("src/test/resources/examples/annotations/left.java");
		File right = new File("src/test/resources/examples/annotations/right.java");

		Diff diff = new AstComparator().compare(left, right);

		assertEquals(1, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Update, "Annotation"));
	}

	@Test
	public void test_diffOfGenericTypeReference_multipleNesting() throws Exception {
		File left = new File("src/test/resources/examples/diffOfGenericTypeReferences/multipleNesting/left.java");
		File right = new File("src/test/resources/examples/diffOfGenericTypeReferences/multipleNesting/right.java");

		Diff diff = new AstComparator().compare(left, right);

		diff.getRootOperations();

		assertEquals(3, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Update, "TypeParameterReference", "A"));
		assertTrue(diff.containsOperation(OperationKind.Move, "TypeReference"));
		assertTrue(diff.containsOperation(OperationKind.Insert, "TypeReference"));
	}
}
