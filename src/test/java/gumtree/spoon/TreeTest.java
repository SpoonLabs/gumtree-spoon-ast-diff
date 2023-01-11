/* *****************************************************************************
 * Copyright 2016 Matias Martinez
 * Copyright (c) 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************/

package gumtree.spoon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.io.TreeIoUtils.TreeSerializer;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import com.google.gson.JsonObject;

import gumtree.spoon.builder.CtWrapper;
import gumtree.spoon.builder.Json4SpoonGenerator;
import gumtree.spoon.builder.Json4SpoonGenerator.JSON_PROPERTIES;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.DiffImpl;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.Launcher;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.path.CtPath;
import spoon.reflect.visitor.CtScanner;

public class TreeTest {

	@Test
	public void test_Path_1() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astLeft = diff.getCtType(fl);

		assertNotNull(astLeft);
		assertEquals("QuickNotepad", astLeft.getSimpleName());

		CtPath pathLeft = astLeft.getPath();
		assertNotNull(pathLeft);

		CtType<?> astRight = diff.getCtType(fr);

		assertNotNull(astRight);
		assertEquals("QuickNotepad", astRight.getSimpleName());
		// No package
		assertEquals("QuickNotepad", astRight.getQualifiedName());

		CtPath pathRight = astRight.getPath();
		assertNotNull(pathRight);

		pathOfStmt(astLeft);
		pathOfStmt(astRight);

	}

	@Test
	public void test_Path_of_affected_nodes() throws Exception {
		AstComparator comparator = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astLeft = comparator.getCtType(fl);

		assertNotNull(astLeft);

		CtType<?> astRight = comparator.getCtType(fr);
		assertNotNull(astRight);

		Diff diffResult = comparator.compare(astLeft, astRight);
		List<Operation> rootOperations = diffResult.getRootOperations();
		getPaths(rootOperations);
		List<Operation> allOperations = diffResult.getAllOperations();
		getPaths(allOperations);

	}

	@Test
	public void test_Path_Scanner_failing_tofix() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astLeft = diff.getCtType(fl);

		assertNotNull(astLeft);
		assertEquals("QuickNotepad", astLeft.getSimpleName());

		CtPath pathLeft = astLeft.getPath();
		assertNotNull(pathLeft);

		CtType<?> astRight = diff.getCtType(fr);

		assertNotNull(astRight);
		assertEquals("QuickNotepad", astRight.getSimpleName());
		// No package in that file
		assertEquals("QuickNotepad", astRight.getQualifiedName());

		CtPath pathRight = astRight.getPath();
		assertNotNull(pathRight);

		PathScanner pscanner = new PathScanner();
		astLeft.accept(pscanner);
		astRight.accept(pscanner);

	}

	@Test
	public void test_Path_ScannerWithSpoon_left() throws Exception {

		Launcher spoon = new Launcher();
		Factory factory = spoon.createFactory();
		spoon.createCompiler(factory,
				SpoonResourceHelper.resources("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java"))
				.build();

		CtType<?> astLeft = factory.Type().get("QuickNotepad");
		assertNotNull(astLeft.getPath());
		assertNotNull(astLeft);
		PathScanner pscanner = new PathScanner();
		astLeft.accept(pscanner);

	}

	@Test
	public void test_Path_ScannerWithSpoon_Right() throws Exception {

		Launcher spoon = new Launcher();
		Factory factory = spoon.createFactory();
		spoon.createCompiler(factory,
				SpoonResourceHelper.resources("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java"))
				.build();

		CtType<?> astLeft = factory.Type().get("QuickNotepad");
		assertNotNull(astLeft.getPath());
		assertNotNull(astLeft);
		PathScanner pscanner = new PathScanner();
		astLeft.accept(pscanner);

	}

	@Test
	public void test_JSON_from_GT() throws Exception {

		Launcher spoon = new Launcher();
		Factory factory = spoon.createFactory();
		spoon.createCompiler(factory,
				SpoonResourceHelper.resources("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java"))
				.build();

		CtType<?> astLeft = factory.Type().get("QuickNotepad");
		SpoonGumTreeBuilder builder = new SpoonGumTreeBuilder();
		Tree generatedTree = builder.getTree(astLeft);

		TreeContext tcontext = new TreeContext();
		tcontext.setRoot(generatedTree);
		TreeSerializer ts = TreeIoUtils.toJson(tcontext);
		String out = ts.toString();
		assertNotNull(out);
	}

	@Test
	public void test_JSON_manual_generation_1() throws Exception {

		Launcher spoon = new Launcher();
		Factory factory = spoon.createFactory();
		spoon.createCompiler(factory,
				SpoonResourceHelper.resources("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java"))
				.build();

		CtType<?> aType = factory.Type().get("QuickNotepad");
		SpoonGumTreeBuilder builder = new SpoonGumTreeBuilder();
		assertNotNull(new Json4SpoonGenerator().getJSONasJsonObject(aType));
	}

	@Test
	public void test_JSON_manual_generation_2() throws Exception {

		Launcher spoon = new Launcher();

		spoon.getFactory().getEnvironment().setCommentEnabled(false);
		Factory factory = spoon.createFactory();
		factory.getEnvironment().setCommentEnabled(false);
		spoon.createCompiler(factory,
				SpoonResourceHelper.resources("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java"))
				.build();

		CtType<?> aType = factory.Type().get("QuickNotepad");
		String jsoNasString = new Json4SpoonGenerator().getJSONasString(aType).trim();
		assertEquals(FileUtils.readFileToString(new File("./src/test/resources/examples/spoon.json")).trim(),
				jsoNasString);
	}

	@Test
	public void test_Path_ScannerWithGTFailing_left() throws Exception {

		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");

		CtType<?> astLeft = diff.getCtType(fl);

		PathScanner pscanner = new PathScanner();

		assertNotNull(astLeft.getPath());
		assertNotNull(astLeft);
		astLeft.accept(pscanner);

	}

	@Test
	public void test_Path_ScannerWithGTFailing_right() throws Exception {

		AstComparator diff = new AstComparator();

		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astRight = diff.getCtType(fr);

		PathScanner pscanner = new PathScanner();

		assertNotNull(astRight.getPath());
		assertNotNull(astRight);
		astRight.accept(pscanner);

	}

	@Test
	public void test_Path_ScannerWithGTFailing_both() throws Exception {

		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");

		CtType<?> astLeft = diff.getCtType(fl);

		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astRight = diff.getCtType(fr);

		PathScanner pscanner = new PathScanner();

		assertNotNull(astLeft.getPath());
		assertNotNull(astLeft);
		astLeft.accept(pscanner);

		assertNotNull(astRight.getPath());
		assertNotNull(astRight);
		astRight.accept(pscanner);

	}

	@Test
	public void test_Path_ScannerWithSpoon_both() throws Exception {

		Launcher spoon = new Launcher();
		Factory factory = spoon.createFactory();
		spoon.createCompiler(factory,
				SpoonResourceHelper.resources("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java"))
				.build();

		CtType<?> astLeft = factory.Type().get("QuickNotepad");

		// So, if we create a new factory, it works.
		// Reusing the same factory, the path fails.
		factory = spoon.createFactory();
		spoon.createCompiler(factory,
				SpoonResourceHelper.resources("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java"))
				.build();

		CtType<?> astRight = factory.Type().get("QuickNotepad");

		PathScanner pscanner = new PathScanner();

		assertNotNull(astLeft.getPath());
		assertNotNull(astLeft);
		astLeft.accept(pscanner);

		assertNotNull(astRight.getPath());
		assertNotNull(astRight);
		astRight.accept(pscanner);

	}

	private void getPaths(List<Operation> rootops) {
		for (Operation<?> op : rootops) {

			CtElement left = op.getSrcNode();
			CtPath pleft = left.getPath();
			assertNotNull(pleft);

			CtElement right = op.getSrcNode();
			CtPath pright = right.getPath();
			assertNotNull(pleft);

		}
	}

	private void pathOfStmt(CtType<?> ast) {
		for (CtMethod method : ast.getAllMethods()) {

			CtPath path = method.getPath();
			assertNotNull(path);

			for (CtStatement stmt : method.getBody().getStatements()) {
				path = stmt.getPath();
				assertNotNull(path);
			}

		}
	}

	private final class PathScanner extends CtScanner {
		@Override
		public void scan(CtElement element) {
			if (element != null) {
				CtPath path = element.getPath();
				assertNotNull(path);

			}
			// to avoid visiting all childs
			super.scan(element);
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void test_packageName_202564() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/t_202564/left_PropPanelModelElement_1.9.java");

		CtType typel = diff.getCtType(fl);
		assertNotNull(typel);
		assertEquals("org.argouml.uml.ui.foundation.core.PropPanelModelElement", typel.getQualifiedName());
		assertEquals("org.argouml.uml.ui.foundation.core", typel.getPackage().getQualifiedName());

	}

	@Test
	public void test_JSON_COLORED() throws Exception {

		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");

		CtType<?> astLeft = diff.getCtType(fl);

		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astRight = diff.getCtType(fr);

		assertNotNull(astLeft);

		assertNotNull(astRight);

		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();
		// Using min = 1, failing
		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		DiffImpl diffC = (DiffImpl) diff.compare(astLeft, astRight, properties);

		TreeContext context = diffC.getContext();

		assertTrue(diffC.getAllOperations().size() > 0);
		assertTrue(diffC.getAllOperations().get(0) instanceof InsertOperation);

		Json4SpoonGenerator jsongen = new Json4SpoonGenerator();

		// Modified
		Tree insertedNode = diffC.getRootOperations().get(0).getAction().getNode();

		JsonObject jsonOb = jsongen.getJSONwithOperations(context, insertedNode, diffC.getAllOperations());

		assertTrue(jsonOb.has(JSON_PROPERTIES.op.toString()));

		assertEquals("\"insert-node\"", jsonOb.get(JSON_PROPERTIES.op.toString()).toString());
	}

	@Test
	public void test_bugissue84_original_failling() throws Exception {
		File fl = new File("src/test/resources/examples/919148/ReplicationRun/919148_ReplicationRun_0_s.java");
		File fr = new File("src/test/resources/examples/919148/ReplicationRun/919148_ReplicationRun_0_t.java");

		AstComparator diff = new AstComparator();

		DiffImpl diffC = (DiffImpl) diff.compare(fl, fr);

		List<Operation> ops = diffC.getAllOperations();

		for (Operation operation : ops) {
			assertNotNull(operation.getSrcNode());
		}

	}

	@Test
	public void test_bugissue84_1_insert() throws Exception {
		File fl = new File("src/test/resources/examples/issue84_1/file_s.java");
		File fr = new File("src/test/resources/examples/issue84_1/file_t.java");

		AstComparator diff = new AstComparator();

		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();
		// Using min = 1, failing
		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		DiffImpl diffC = (DiffImpl) diff.compare(fl, fr, properties);

		List<Operation> ops = diffC.getAllOperations();

		// to change a method to static means to change the type accesses that invoke
		// that method
		assertTrue(ops.size() > 0);

		for (Operation operation : ops) {
			assertNotNull(operation.getSrcNode());
		}

		Optional<Operation> findInsert = ops.stream().filter(e -> e instanceof InsertOperation).findFirst();

		assertTrue(findInsert.isPresent());

		Operation op1 = findInsert.get();
		assertTrue(op1 instanceof InsertOperation);

		assertTrue(op1.getSrcNode() instanceof CtWrapper);

		assertEquals("static", ((CtWrapper) op1.getSrcNode()).getValue().toString());

	}

	@Test
	public void test_bugissue84_2_update() throws Exception {
		File fl = new File("src/test/resources/examples/issue84_2/file_s.java");
		File fr = new File("src/test/resources/examples/issue84_2/file_t.java");

		AstComparator diff = new AstComparator();

		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();
		// Using min = 1, failing
		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		DiffImpl diffC = (DiffImpl) diff.compare(fl, fr, properties);

		List<Operation> ops = diffC.getAllOperations();
		assertEquals(1, ops.size());
		System.out.println(ops);

		for (Operation operation : ops) {
			assertNotNull(operation.getSrcNode());
		}

		Operation op1 = ops.get(0);
		assertTrue(op1 instanceof UpdateOperation);

		assertTrue(op1.getSrcNode() instanceof CtWrapper);

		assertEquals("public", ((CtWrapper) op1.getSrcNode()).getValue().toString());

		assertEquals("protected", ((CtWrapper) op1.getDstNode()).getValue().toString());

	}

	@Test
	public void test_bug_Possition() throws Exception {
		AstComparator comparator = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astLeft = comparator.getCtType(fl);

		assertNotNull(astLeft);

		CtType<?> astRight = comparator.getCtType(fr);
		assertNotNull(astRight);

		Diff diffResult = comparator.compare(astLeft, astRight);
		List<Operation> rootOperations = diffResult.getRootOperations();
		getPaths(rootOperations);
		List<Operation> allOperations = diffResult.getAllOperations();
		getPaths(allOperations);

		assertEquals(1, rootOperations.size());

		SourcePosition position = rootOperations.get(0).getSrcNode().getPosition();
		assertTrue(position.getLine() > 0);
		assertEquals(113, position.getLine());

		assertTrue(!(position instanceof NoSourcePosition));

	}

	@Test
	public void test_bug_Possition_from_String() {
		String c1 = "" + "class X {\n" + "public void foo() {\n" + " int x = 0;\n" + "}" + "};";

		String c2 = "" + "class X {\n" + "public void foo() {\n" + " int x = 1;\n" + "}" + "};";

		AstComparator diff = new AstComparator();
		Diff editScript = diff.compare(c1, c2);
		assertTrue(editScript.getRootOperations().size() == 1);

		List<Operation> rootOperations = editScript.getRootOperations();

		assertEquals(1, rootOperations.size());

		SourcePosition position = rootOperations.get(0).getSrcNode().getPosition();
		assertTrue(!(position instanceof NoSourcePosition));

		assertTrue(position.getLine() > 0);
		assertEquals(3, position.getLine());

	}

	@Test
	public void test_getTree_labelExistsForAnnotations() {
		String codeWithAnnotation = "class A { @Override public boolean equals(Object obj) { return false; }";

		AstComparator comparator = new AstComparator();
		CtType<?> spoonType = comparator.getCtType(codeWithAnnotation);

		final SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
		Tree root = scanner.getTree(spoonType);

		Tree annotationNode = root.getChild(0).getChild(0).getChild(2);
		assertEquals("java.lang.Override", annotationNode.getLabel());
	}

	@Test
	public void test_nestingLevelOfSuperType() {
		CtClass<?> spoonClass = Launcher.parseClass("class Main extends SuperClass");
		Tree root = new SpoonGumTreeBuilder().getTree(spoonClass);
		Tree klass = root.getChild(0);
		Tree superClass = klass.getChild(0);

		assertEquals("SuperClass", superClass.getLabel());
		assertEquals(superClass.getMetrics().depth, klass.getMetrics().depth + 1);
		assertEquals(0, superClass.getDescendants().size());
	}

	@Test
	public void test_levelsOfNestingOfTypeArguments() throws Exception {
		File nestedList = new File(
				"src/test/resources/examples/diffOfGenericTypeReferences/multipleNesting/right.java");

		AstComparator comparator = new AstComparator();
		SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
		Tree root = scanner.getTree(comparator.getCtType(nestedList));
		String listLabel = "java.util.List";
		String stringLabel = "java.lang.String";

		Tree field = root.getChild(0).getChild(3);
		List<String> childLabels = field.getDescendants().stream().map(Tree::getLabel).collect(Collectors.toList());
		long listLabelsCount = childLabels.stream().filter(listLabel::equals).count();
		long stringLabelsCount = childLabels.stream().filter(stringLabel::equals).count();

		assertEquals("Not enough List containers", 4, listLabelsCount);
		assertEquals("Not enough String type arguments", 1, stringLabelsCount);
		assertEquals("There should only be list and string labels", childLabels.size(),
				listLabelsCount + stringLabelsCount);
	}

	@Test
	public void test_superInterfacesShouldBeDescendantsOfClassNode() {
		CtClass<?> spoonClass = Launcher.parseClass("class Example implements A, B, C { }");
		Tree root = new SpoonGumTreeBuilder().getTree(spoonClass);
		Tree superInterfaces = root.getChild(0).getChild(0);

		assertEquals(3, superInterfaces.getDescendants().size());
	}

	@Test
	public void test_nestedReturnTypeOfMethodShouldGetParsed() throws Exception {
		File file = new File("src/test/resources/examples/NestedReturnType.java");
		AstComparator comparator = new AstComparator();
		SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
		Tree root = scanner.getTree(comparator.getCtType(file));
		Tree method = root.getDescendants().stream().filter(Tree -> Tree.getLabel().equals("getIntegers")).findFirst()
				.get();

		Tree returnType = method.getChild(0);
		assertEquals(1, returnType.getDescendants().size());
		assertEquals("java.lang.Integer", returnType.getChild(0).getLabel());
	}

	private void assertPosition(SourcePosition position, int line, int start, int end) {
		assertNotNull(position);
		assertEquals(line, position.getLine());
		assertEquals(start, position.getSourceStart());
		assertEquals(end, position.getSourceEnd());
	}

	@Test
	public void test_accessModifierWrapperInFile_shouldHaveSourcePosition() throws Exception {
		// arrange
		File fl = new File("src/test/resources/examples/position/WrapperLeft.java");
		File fr = new File("src/test/resources/examples/position/WrapperRight.java");

		// act
		Diff diff = new AstComparator().compare(fl, fr);

		// assert
		CtElement privateModifier = diff.getRootOperations().get(0).getSrcNode();
		assertPosition(privateModifier.getPosition(), 18, 789, 795);

		CtElement synchronizedModifier = diff.getRootOperations().get(1).getSrcNode();
		assertPosition(synchronizedModifier.getPosition(), 18, 804, 815);
	}

	@Test
	public void test_accessModifierWrapperInCodeString_shouldHaveSourcePosition() {
		// arrange
		String left = "class A { static void a() {} }";
		String right = "class A { private static synchronized void a() {} }";

		// act
		Diff diff = new AstComparator().compare(left, right);

		// assert
		CtElement privateModifier = diff.getRootOperations().get(0).getSrcNode();
		assertPosition(privateModifier.getPosition(), 1, 10, 16);

		CtElement synchronizedModifier = diff.getRootOperations().get(1).getSrcNode();
		assertPosition(synchronizedModifier.getPosition(), 1, 25, 36);
	}

	@Test
	public void test_accessModifierVirtualElementInFile_shouldHaveSourcePosition() throws Exception {
		// arrange
		File fl = new File("src/test/resources/examples/position/VirtualElementLeft.java");
		File fr = new File("src/test/resources/examples/position/VirtualElementRight.java");

		// act
		Diff diff = new AstComparator().compare(fl, fr);

		// assert
		CtElement accessModifiers = diff.getRootOperations().get(0).getSrcNode();
		assertPosition(accessModifiers.getPosition(), 18, 789, 802);
	}

	@Test
	public void test_accessModifierVirtualElementInCodeString_shouldHaveSourcePosition() {
		// arrange
		String left = "class A { void a() {} }";
		String right = "class A { private static void a() {} }";

		// act
		Diff diff = new AstComparator().compare(left, right);

		// assert
		CtElement accessModifiers = diff.getRootOperations().get(0).getSrcNode();
		assertPosition(accessModifiers.getPosition(), 1, 10, 23);
	}
}
