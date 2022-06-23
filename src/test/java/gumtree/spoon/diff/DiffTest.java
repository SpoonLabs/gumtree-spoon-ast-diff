package gumtree.spoon.diff;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.gumtreediff.matchers.CompositeMatchers;
import org.junit.Ignore;
import org.junit.Test;

import gumtree.spoon.builder.CtVirtualElement;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import spoon.Launcher;
import spoon.reflect.code.CtLiteral;
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
		assertEquals(1, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Update, "TYPE_ARGUMENT"));
	}

	@Test
	public void test_diffOfGenericTypeReference_builtInTypeToTypeParameter() throws Exception {
		File left = new File("src/test/resources/examples/diffOfGenericTypeReferences/builtInTypeToTypeParameter/left.java");
		File right = new File("src/test/resources/examples/diffOfGenericTypeReferences/builtInTypeToTypeParameter/right.java");

		Diff diff = new AstComparator().compare(left, right);
		assertEquals(1, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Update, "TYPE_ARGUMENT"));
	}

	@Test
	public void test_diffOfGenericTypeReference_typeParameterToBuiltInType() throws Exception {
		File left = new File("src/test/resources/examples/diffOfGenericTypeReferences/typeParameterToBuiltInType/left.java");
		File right = new File("src/test/resources/examples/diffOfGenericTypeReferences/typeParameterToBuiltInType/right.java");

		Diff diff = new AstComparator().compare(left, right);
		assertEquals(1, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Update, "TYPE_ARGUMENT"));
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

		assertEquals(3, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Update, "TYPE_ARGUMENT", "A"));
		assertTrue(diff.containsOperation(OperationKind.Move, "TYPE_ARGUMENT"));
		assertTrue(diff.containsOperation(OperationKind.Insert, "TYPE_ARGUMENT"));
	}

	@Test
	public void test_diffOfSuperInterfaces_class() throws Exception {
		File left = new File("src/test/resources/examples/superInterfaces/class/left.java");
		File right = new File("src/test/resources/examples/superInterfaces/class/right.java");

		Diff diff = new AstComparator().compare(left, right);

		assertTrue(diff.containsOperation(OperationKind.Insert, "INTERFACE", "A"));
		assertTrue(diff.containsOperation(OperationKind.Insert, "INTERFACE", "D"));
		assertTrue(diff.containsOperation(OperationKind.Insert, "TYPE_ARGUMENT", "T"));
		// outer list is inserted
		assertTrue(diff.containsOperation(OperationKind.Insert, "TYPE_ARGUMENT", "java.util.List"));
		// initial list is nested inside the above inserted list
		assertTrue(diff.containsOperation(OperationKind.Move, "TYPE_ARGUMENT", "java.util.List"));

	}

	@Test
	public void test_diffOfSuperInterfaces_interface() throws Exception {
		File left = new File("src/test/resources/examples/superInterfaces/interface/left.java");
		File right = new File("src/test/resources/examples/superInterfaces/interface/right.java");

		Diff diff = new AstComparator().compare(left, right);

		assertTrue(diff.containsOperation(OperationKind.Delete, "INTERFACE", "A"));
		assertTrue(diff.containsOperation(OperationKind.Insert, "INTERFACE", "D"));
		assertTrue(diff.containsOperation(OperationKind.Insert, "TYPE_ARGUMENT", "T"));
		// outer list is inserted
		assertTrue(diff.containsOperation(OperationKind.Insert, "TYPE_ARGUMENT", "java.util.List"));
		// initial list is nested inside the above inserted list
		assertTrue(diff.containsOperation(OperationKind.Move, "TYPE_ARGUMENT", "java.util.List"));
	}

	@Test
	public void test_diffOfSuperInterfaces_insertionOfRootNodeOfInterface() throws Exception {
		File left = new File("src/test/resources/examples/superInterfaces/insertion/left.java");
		File right = new File("src/test/resources/examples/superInterfaces/insertion/right.java");

		Diff diff = new AstComparator().compare(left, right);

		// assert that only the root of super interfaces is inserted
		assertEquals(1, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Insert, "SUPER_INTERFACES"));

		// verify children of the inserted root node
		CtVirtualElement superInterfaceRoot = (CtVirtualElement) diff.getRootOperations().get(0).getSrcNode();
		assertArrayEquals(new String[]{"A", "B"}, superInterfaceRoot.getChildren().stream().map(Object::toString).toArray());
	}

	@Test
	public void test_diffOfThrownTypes_insertionOfRootNodeOfThrowable() throws Exception {
		File left = new File("src/test/resources/examples/thrownTypes/left.java");
		File right = new File("src/test/resources/examples/thrownTypes/right.java");

		Diff diff = new AstComparator().compare(left, right);

		// assert that only the root of throwables is inserted
		assertEquals(1, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Insert, "THROWN_TYPES"));

		// verify children of the inserted root node
		CtVirtualElement thrownTypeRoot = (CtVirtualElement) diff.getRootOperations().get(0).getSrcNode();
		assertNotNull(thrownTypeRoot);
		assertArrayEquals(new String[] {
				"java.lang.ClassNotFoundException",
				"java.lang.ClassCastException"
		}, thrownTypeRoot.getChildren().stream().map(Object::toString).toArray());
	}

	@Test
	public void test_diffOfAnnotations_insertionOfValues() throws Exception {
		File left = new File("src/test/resources/examples/annotationValues/insert/left.java");
		File right = new File("src/test/resources/examples/annotationValues/insert/right.java");

		Diff diff = new AstComparator().compare(left, right);

		assertEquals(2, diff.getRootOperations().size());
		assertTrue(diff.containsOperation(OperationKind.Insert, "ANNOTATION_VALUE", "type=\"int\""));
		assertTrue(diff.containsOperation(OperationKind.Insert, "ANNOTATION_VALUE", "value=\"41\""));
	}

	@Test
	public void test_diffOfAnnotations_firstInsertOfValue() throws Exception {
		File left = new File("src/test/resources/examples/annotationValues/firstInsert/left.java");
		File right = new File("src/test/resources/examples/annotationValues/firstInsert/right.java");

		Diff diff = new AstComparator().compare(left, right);

		assertEquals(1, diff.getRootOperations().size());
		Map<String, CtLiteral<?>> expectedValues = new LinkedHashMap<>();
		expectedValues.put("since", new Launcher().getFactory().createLiteral("4.2"));

		CtVirtualElement annotation = (CtVirtualElement) diff.getRootOperations().get(0).getSrcNode();
		List<?> actualValues = Arrays.asList(annotation.getChildren().toArray());
		assertThat(new ArrayList<>(expectedValues.entrySet()), equalTo(actualValues));

	}

	// ToDo
	@Ignore("Can be fixed after #154")
	@Test
	public void test_diffOfAnnotationValues_updateMethod() throws Exception {
		File left = new File("src/test/resources/examples/annotationValues/updateMethod/left.java");
		File right = new File("src/test/resources/examples/annotationValues/updateMethod/right.java");

		Diff diff = new AstComparator().compare(left, right);

		assertEquals(2, diff.getRootOperations().size());
		assertTrue(diff.containsOperations(OperationKind.Update, "AnnotationMethod", "a", "b"));
		assertTrue(diff.containsOperations(OperationKind.Update, "ANNOTATION_VALUE", "a=\"1\"", "b=\"1\""));
	}

	@Test
	public void test_diffInTypeCasts_primitiveTypes() throws Exception {
		// arrange
		File left = new File("src/test/resources/examples/typeCast/primitiveType/left.java");
		File right = new File("src/test/resources/examples/typeCast/primitiveType/right.java");

		// act
		Diff diff = new AstComparator().compare(left, right);

		// assert
		assertThat(diff.getRootOperations().size(), equalTo(1));
		assertTrue(diff.containsOperation(OperationKind.Insert, "TYPE_CAST", "double"));
	}

	@Test
	public void test_diffInTypeCasts_genericTypes() throws Exception {
		// arrange
		File left = new File("src/test/resources/examples/typeCast/genericType/left.java");
		File right = new File("src/test/resources/examples/typeCast/genericType/right.java");

		// act
		Diff diff = new AstComparator().compare(left, right);

		// assert
		assertThat(diff.getRootOperations().size(), equalTo(1));
		assertTrue(diff.containsOperation(OperationKind.Insert, "TYPE_CAST", "java.util.Map"));
		assertThat(diff.getAllOperations().size(), equalTo(3));
	}

	@Test
	public void test_fineGrainedDiff_fieldReadUpdateInsideConstructorCall() throws Exception {
		// arrange
		File left = new File("src/test/resources/examples/patch1-Math-20-Elixir-plausible/left.java");
		File right = new File("src/test/resources/examples/patch1-Math-20-Elixir-plausible/right.java");

		// act
		DiffConfiguration configuration = new DiffConfiguration();
		configuration.setMatcher(new CompositeMatchers.HybridGumtree());
		Diff diff = new AstComparator().compare(left, right, configuration);

		// assert
		assertThat(diff.getRootOperations().size(), equalTo(1));
		assertTrue(diff.containsOperations(OperationKind.Update, "CtFieldRead", "DEFAULT_MAXITERATIONS", "DEFAULT_CHECKFEASABLECOUNT"));

	}
}
