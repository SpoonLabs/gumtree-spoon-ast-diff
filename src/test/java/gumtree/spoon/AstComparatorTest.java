package gumtree.spoon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.Mapping;

import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.ActionClassifier;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.DiffImpl;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.support.compiler.VirtualFile;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;
import spoon.support.compiler.jdt.JDTSnippetCompiler;

/**
 * Test Spoon Diff
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class AstComparatorTest {

	@Test
	public void propertiesCorrectlySet() {
		new AstComparator();
	}

	@Test
	public void testgetCtType() throws Exception {
		final Factory factory = new Launcher().getFactory();
		String c1 = "package spoon1.test; " //
				+ "class X {" //
				+ "public void foo0() {" //
				+ " int x = 0;" //
				+ "}" //
				+ "}";
		assertTrue(getCtType(factory, c1) != null);
	}

	@Test
	public void testAnalyzeStringString() {
		String c1 = "" + "class X {" + "public void foo0() {" + " int x = 0;" + "}" + "};";

		String c2 = "" + "class X {" + "public void foo1() {" + " int x = 0;" + "}" + "};";

		AstComparator diff = new AstComparator();
		Diff editScript = diff.compare(c1, c2);
		assertTrue(editScript.getRootOperations().size() == 1);
	}

	@Test
	public void exampleInsertAndUpdate() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/test1/TypeHandler1.java");
		File fr = new File("src/test/resources/examples/test1/TypeHandler2.java");

		Diff result = diff.compare(fl, fr);
		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertEquals(4, actions.size());

		result.debugInformation();

		CtElement ancestor = result.commonAncestor();
		assertTrue(ancestor instanceof CtClass);
		assertTrue(result.containsOperation(OperationKind.Insert, "Invocation"));

		assertFalse(result.containsOperation(OperationKind.Delete, "Invocation"));
		assertFalse(result.containsOperation(OperationKind.Update, "Invocation"));
	}

	@Test
	public void exampleSingleUpdate() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/test2/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test2/CommandLine2.java");

		Diff result = diff.compare(fl, fr);
		List<Operation> actions = result.getRootOperations();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Update, "Literal"/* "PAR-Literal" */));
	}

	@Test
	public void exampleRemoveMethod() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/test3/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test3/CommandLine2.java");

		Diff result = diff.compare(fl, fr);
		// result.debugInformation();
		// commenting the assertion on the number of actions
		// we now have three actions, with two updates of invocations because of binding
		// to the ol/new method
		// while it is not visible in the AST, this is indeed a change in the behavior
		// it means that the AST diff in this case also captures something deeper
		// assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Delete, "Method"));
	}

	@Test
	public void exampleInsert() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/test4/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test4/CommandLine2.java");

		Diff result = diff.compare(fl, fr);
		List<Operation> actions = result.getRootOperations();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Insert, "Method", "resolveOptionNew"));
	}

	@Test
	public void testMain() throws Exception {
		File fl = new File("src/test/resources/examples/test4/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test4/CommandLine2.java");
		AstComparator.main(new String[] { fl.getAbsolutePath(), fr.getAbsolutePath() });
	}

	@Test
	public void testContent() throws Exception {
		final Factory factory = new Launcher().createFactory();
		File fl = new File("src/test/resources/examples/test4/CommandLine1.java");
		assertNotNull(getSpoonType(factory, readFile(fl)));
	}

	public static CtType<?> getCtType(Factory factory, String content) {
		SpoonModelBuilder compiler = new JDTBasedSpoonCompiler(factory);
		compiler.addInputSource(new VirtualFile(content, "/test"));
		compiler.build();
		return factory.Type().getAll().get(0);
	}

	private static CtType getSpoonType(Factory factory, String content) {
		try {
			canBuild(factory, content);
		} catch (Exception e) {
			// must fails
		}
		List<CtType<?>> types = factory.Type().getAll();
		if (types.isEmpty()) {
			throw new RuntimeException("No Type was created by spoon");
		}
		CtType spt = types.get(0);
		spt.getPackage().getTypes().remove(spt);

		return spt;
	}

	private static void canBuild(Factory factory, String content) {
		SpoonModelBuilder builder = new JDTSnippetCompiler(factory, content);
		try {
			builder.build();
		} catch (Exception e) {
			throw new RuntimeException("snippet compilation error while compiling: " + content, e);
		}
	}

	private static String readFile(File f) throws IOException {
		FileReader reader = new FileReader(f);
		char[] chars = new char[(int) f.length()];
		reader.read(chars);
		String content = new String(chars);
		reader.close();
		return content;
	}

	@Test
	public void testJDTBasedSpoonCompiler() {
		String content1 = "package spoon1.test; " //
				+ "class X {" //
				+ "public void foo0() {" //
				+ " int x = 0;" //
				+ "}" + "}";

		Factory factory = new Launcher().createFactory();

		SpoonModelBuilder compiler = new JDTSnippetCompiler(factory, content1);
		compiler.build();
		CtClass<?> clazz1 = (CtClass<?>) factory.Type().getAll().get(0);

		Assert.assertNotNull(clazz1);
	}

	@Test
	public void test5() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/test5/left_LmiInitialContext_1.5.java");
		File fr = new File("src/test/resources/examples/test5/right_LmiInitialContext_1.6.java");
		Diff result = diff.compare(fl, fr);
		List<Operation> actions = result.getRootOperations();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Update, "BinaryOperator", "AND"));
	}

	@Test
	public void test6() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/test6/A.java");
		File fr = new File("src/test/resources/examples/test6/B.java");
		Diff result = diff.compare(fl, fr);
		List<Operation> actions = result.getRootOperations();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Delete, "Parameter", "i"));
	}

	@Test
	public void test7() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/test7/left_QuickNotepad_1.13.java
		// src/test/resources/examples/test7/right_QuickNotepad_1.14.java
		File fl = new File("src/test/resources/examples/test7/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/test7/right_QuickNotepad_1.14.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		System.out.println(actions);
		assertEquals(2, actions.size());
		assertTrue(result.containsOperation(OperationKind.Delete, "Invocation", "addKeyListener"));
		assertTrue(result.containsOperation(OperationKind.Delete, "Class", "KeyHandler"));

		CtElement ancestor = result.commonAncestor();
		assertTrue(ancestor instanceof CtClass);
		assertEquals("QuickNotepad", ((CtClass) ancestor).getSimpleName());

	}

	@Test
	public void test8() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/test8/left.java");
		File fr = new File("src/test/resources/examples/test8/right.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		assertEquals(1, actions.size());
		assertTrue(actions.toString(),
				result.containsOperation(OperationKind.Update, "VARIABLE_TYPE", "java.lang.Throwable"));
	}

	@Test
	public void test9() throws Exception {
		// contract: we detect local variable changes too
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/test9/left.java");
		File fr = new File("src/test/resources/examples/test9/right.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(actions.toString(), result.containsOperation(OperationKind.Update, "VARIABLE_TYPE", "boolean"));
	}

	@Test
	public void test_t_286700() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_286700/left_CmiContext_1.2.java
		// src/test/resources/examples/t_286700/right_CmiContext_1.3.java
		File fl = new File("src/test/resources/examples/t_286700/left_CmiContext_1.2.java");
		File fr = new File("src/test/resources/examples/t_286700/right_CmiContext_1.3.java");
		Diff result = diff.compare(fl, fr);

		// result.debugInformation();
		assertTrue(result.containsOperation(OperationKind.Insert, "Method", "getObjectPort"));
		// commented for the same reason as exampleRemoveMethod
		// assertEquals(1, actions.size());

	}

	@Test
	public void test_t_202564() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_202564/left_PropPanelModelElement_1.9.java
		// src/test/resources/examples/t_202564/right_PropPanelModelElement_1.10.java
		File fl = new File("src/test/resources/examples/t_202564/left_PropPanelModelElement_1.9.java");
		File fr = new File("src/test/resources/examples/t_202564/right_PropPanelModelElement_1.10.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Insert, "Field", "_assocEndRoleIcon"));
	}

	@Test
	public void test_t_204225() throws Exception {
		AstComparator diff = new AstComparator();
		// meld
		// src/test/resources/examples/t_204225/left_UMLModelElementStereotypeComboBoxModel_1.3.java
		// src/test/resources/examples/t_204225/right_UMLModelElementStereotypeComboBoxModel_1.4.java
		File fl = new File("src/test/resources/examples/t_204225/left_UMLModelElementStereotypeComboBoxModel_1.3.java");
		File fr = new File(
				"src/test/resources/examples/t_204225/right_UMLModelElementStereotypeComboBoxModel_1.4.java");
		Diff result = diff.compare(fl, fr);

		CtElement ancestor = result.commonAncestor();
		assertTrue(ancestor instanceof CtReturn);

		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsOperation(OperationKind.Insert, "BinaryOperator", "OR"));
		assertTrue(result.containsOperation(OperationKind.Move, "BinaryOperator", "AND"));

	}

	@Test
	public void test_t_208618() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_208618/left_PropPanelUseCase_1.39.java
		// src/test/resources/examples/t_208618/right_PropPanelUseCase_1.40.java
		File fl = new File("src/test/resources/examples/t_208618/left_PropPanelUseCase_1.39.java");
		File fr = new File("src/test/resources/examples/t_208618/right_PropPanelUseCase_1.40.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Insert, "Invocation", "addField"));

	}

	@Test
	public void test_t_209184() throws Exception {
		AstComparator diff = new AstComparator();
		// meld
		// src/test/resources/examples/t_209184/left_ActionCollaborationDiagram_1.28.java
		// src/test/resources/examples/t_209184/right_ActionCollaborationDiagram_1.29.java
		File fl = new File("src/test/resources/examples/t_209184/left_ActionCollaborationDiagram_1.28.java");
		File fr = new File("src/test/resources/examples/t_209184/right_ActionCollaborationDiagram_1.29.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "Invocation", "getTarget"));

		UpdateOperation updateOp = (UpdateOperation) actions.get(0);
		CtElement dst = updateOp.getDstNode();
		assertNotNull(dst);
		assertTrue(CtInvocation.class.isInstance(dst));
		assertEquals(((CtInvocation) dst).getExecutable().toString(), "getModelTarget()");

	}

	@Test
	public void test_t_209184_buggy_allopsNPE() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/t_209184/left_ActionCollaborationDiagram_1.28.java");
		File fr = new File("src/test/resources/examples/t_209184/right_ActionCollaborationDiagram_1.29.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getAllOperations();
		assertEquals(1, actions.size());
		// assertTrue(result.containsOperation(OperationKind.Update, "Invocation",
		// "#getTarget()"));
		assertTrue(result.containsOperation(OperationKind.Update, "Invocation", "getTarget"));

		UpdateOperation updateOp = (UpdateOperation) actions.get(0);
		CtElement dst = updateOp.getDstNode();
		assertNotNull(dst);
		assertTrue(CtInvocation.class.isInstance(dst));
		assertEquals(((CtInvocation) dst).getExecutable().toString(), "getModelTarget()");
	}

	@Test
	public void test_t_211903() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_211903/left_MemberFilePersister_1.4.java
		// src/test/resources/examples/t_211903/right_MemberFilePersister_1.5.java
		File fl = new File("src/test/resources/examples/t_211903/left_MemberFilePersister_1.4.java");
		File fr = new File("src/test/resources/examples/t_211903/right_MemberFilePersister_1.5.java");
		Diff result = diff.compare(fl, fr);

		// result.debugInformation();

		CtElement ancestor = result.commonAncestor();
		assertTrue(ancestor instanceof CtConstructorCall);
		assertEquals(88, ancestor.getPosition().getLine());

		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertTrue(
				result.containsOperation(OperationKind.Update, "ConstructorCall", "java.io.FileReader(java.io.File)"));
		assertTrue(result.containsOperation(OperationKind.Insert, "ConstructorCall",
				"java.io.InputStreamReader(java.io.InputStream,java.lang.String)"));

		// additional checks on low-level actions
		assertTrue(result.containsOperations(result.getAllOperations(), OperationKind.Insert, "Literal", "\"UTF-8\""));

		// the change is in the local variable declaration
		CtElement elem = actions.get(0).getNode();
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtLocalVariable.class));
	}

	@Test
	public void test_t_212496() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_212496/left_CoreHelperImpl_1.29.java
		// src/test/resources/examples/t_212496/right_CoreHelperImpl_1.30.java
		File fl = new File("src/test/resources/examples/t_212496/left_CoreHelperImpl_1.29.java");
		File fr = new File("src/test/resources/examples/t_212496/right_CoreHelperImpl_1.30.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Insert, "Method", "setEnumerationLiterals"));
	}

	@Test
	public void test_t_214116() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_214116/left_Modeller_1.134.java
		// src/test/resources/examples/t_214116/right_Modeller_1.135.java
		File fl = new File("src/test/resources/examples/t_214116/left_Modeller_1.134.java");
		File fr = new File("src/test/resources/examples/t_214116/right_Modeller_1.135.java");
		Diff result = diff.compare(fl, fr);

		CtElement ancestor = result.commonAncestor();
		assertTrue(ancestor instanceof CtBinaryOperator);

		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsOperation(OperationKind.Update, "Literal", "\" \""));

		// the change is in a throw
		CtElement elem = actions.get(0).getNode();
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtThrow.class));

	}

	@Test
	public void test_t_214614() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_214614/left_JXButtonGroupPanel_1.2.java
		// src/test/resources/examples/t_214614/right_JXButtonGroupPanel_1.3.java
		File fl = new File("src/test/resources/examples/t_214614/left_JXButtonGroupPanel_1.2.java");
		File fr = new File("src/test/resources/examples/t_214614/right_JXButtonGroupPanel_1.3.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertEquals(2, actions.size());
		assertTrue(result.containsOperation(OperationKind.Delete, "Invocation", "setFocusTraversalPolicyProvider"));
	}

	@Test
	public void test_t_220985() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_220985/left_Server_1.20.java
		// src/test/resources/examples/t_220985/right_Server_1.21.java
		File fl = new File("src/test/resources/examples/t_220985/left_Server_1.20.java");
		File fr = new File("src/test/resources/examples/t_220985/right_Server_1.21.java");
		Diff result = diff.compare(fl, fr);

		result.getRootOperations();
		// result.debugInformation();
		assertTrue(result.containsOperation(OperationKind.Insert, "Conditional"));

		// TODO the delete literal "." found could also be a move to the new
		// conditional, so we don't specify this
		// this is the case if gumtree.match.gt.minh" = "0" (but bad for other tests)
	}

	@Test
	public void test_t_221070() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_221070/left_Server_1.68.java
		// src/test/resources/examples/t_221070/right_Server_1.69.java
		File fl = new File("src/test/resources/examples/t_221070/left_Server_1.68.java");
		File fr = new File("src/test/resources/examples/t_221070/right_Server_1.69.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Delete, "Break"));
	}

	@Test
	public void test_t_221295() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_221295/left_Board_1.5.java
		// src/test/resources/examples/t_221295/right_Board_1.6.java
		File fl = new File("src/test/resources/examples/t_221295/left_Board_1.5.java");
		File fr = new File("src/test/resources/examples/t_221295/right_Board_1.6.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Update, "BinaryOperator", "GT"));

		CtElement elem = actions.get(0).getNode();
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtReturn.class));

	}

	@Test
	public void test_t_221966() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_221966/left_TurnOrdered_1.3.java
		// src/test/resources/examples/t_221966/right_TurnOrdered_1.4.java
		File fl = new File("src/test/resources/examples/t_221966/left_TurnOrdered_1.3.java");
		File fr = new File("src/test/resources/examples/t_221966/right_TurnOrdered_1.4.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Delete, "Invocation", "println"));
	}

	@Test
	public void test_t_221343() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_221343/left_Server_1.186.java
		// src/test/resources/examples/t_221343/right_Server_1.187.java
		File fl = new File("src/test/resources/examples/t_221343/left_Server_1.186.java");
		File fr = new File("src/test/resources/examples/t_221343/right_Server_1.187.java");

		GumtreeProperties properties = new GumtreeProperties();

		properties = new GumtreeProperties();
		// Using min = 1, the imports and package declaration are mapped.
		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		// assertTrue(result.containsOperation(OperationKind.Update, "Invocation",
		// "java.util.Vector#remove(int)"));
		assertTrue(result.containsOperation(OperationKind.Update, "Invocation", "remove"));
	}

	@Test
	public void test_t_221345() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_221345/left_Server_1.187.java
		// src/test/resources/examples/t_221345/right_Server_1.188.java
		File fl = new File("src/test/resources/examples/t_221345/left_Server_1.187.java");
		File fr = new File("src/test/resources/examples/t_221345/right_Server_1.188.java");

		GumtreeProperties properties = new GumtreeProperties();

		properties = new GumtreeProperties();
		// Using min = 1, the imports and package declaration are mapped.
		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Update, "Invocation", "removeElement"));
	}

	@Test
	public void test_t_221422() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_221422/left_Server_1.227.java
		// src/test/resources/examples/t_221422/right_Server_1.228.java
		File fl = new File("src/test/resources/examples/t_221422/left_Server_1.227.java");
		File fr = new File("src/test/resources/examples/t_221422/right_Server_1.228.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		// as of Spoon 7.1, the generics are resolved in the signature
		// assertTrue(
		// result.containsOperation(OperationKind.Update, "Invocation",
		// "java.util.Vector#add(java.lang.Object)"));
	}

	@Test
	public void test_t_221958() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_221958/left_TilesetManager_1.22.java
		// src/test/resources/examples/t_221958/right_TilesetManager_1.23.java
		File fl = new File("src/test/resources/examples/t_221958/left_TilesetManager_1.22.java");
		File fr = new File("src/test/resources/examples/t_221958/right_TilesetManager_1.23.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Insert, "Literal", "null"));

		CtElement elem = actions.get(0).getNode();
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtReturn.class));

	}

	@Test
	public void test_t_222361() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_222361/left_CommonSettingsDialog_1.22.java
		// src/test/resources/examples/t_222361/right_CommonSettingsDialog_1.23.java
		File fl = new File("src/test/resources/examples/t_222361/left_CommonSettingsDialog_1.22.java");
		File fr = new File("src/test/resources/examples/t_222361/right_CommonSettingsDialog_1.23.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Update, "Literal", "\"By holding down CTL and dragging.\""));
	}

	@Test
	public void test_t_222399() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_222399/left_TdbFile_1.7.java
		// src/test/resources/examples/t_222399/right_TdbFile_1.8.java
		File fl = new File("src/test/resources/examples/t_222399/left_TdbFile_1.7.java");
		File fr = new File("src/test/resources/examples/t_222399/right_TdbFile_1.8.java");
		Diff result = diff.compare(fl, fr);

		CtElement ancestor = result.commonAncestor();
		assertTrue(ancestor instanceof CtIf);
		assertEquals(229, ancestor.getPosition().getLine());

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(3, actions.size());
		assertEquals(229, ancestor.getPosition().getLine());

		assertTrue(result.containsOperation(OperationKind.Update, "Invocation", "equals"));
		assertTrue(result.containsOperation(OperationKind.Insert, "BinaryOperator", "NE"));
		assertTrue(result.containsOperation(OperationKind.Move, "Invocation", "equals"));

		// updated the if condition
		CtElement elem = actions.get(0).getNode();
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtIf.class));

	}

	@Test
	public void test_t_222884() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_222884/left_MechView_1.21.java
		// src/test/resources/examples/t_222884/right_MechView_1.22.java
		File fl = new File("src/test/resources/examples/t_222884/left_MechView_1.21.java");
		File fr = new File("src/test/resources/examples/t_222884/right_MechView_1.22.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Insert, "Invocation", "append"));
	}

	@Test
	public void test_t_222894() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_222894/left_Client_1.150.java
		// src/test/resources/examples/t_222894/right_Client_1.151.java
		File fl = new File("src/test/resources/examples/t_222894/left_Client_1.150.java");
		File fr = new File("src/test/resources/examples/t_222894/right_Client_1.151.java");
		Diff result = diff.compare(fl, fr);

		CtElement ancestor = result.commonAncestor();
		assertTrue(ancestor instanceof CtIf);

		result.getRootOperations();
		result.debugInformation();
		assertTrue(result.containsOperation(OperationKind.Insert, "BinaryOperator", "AND"));

		// TODO there is a move that is not detected but should be
		// assertTrue(result.containsOperation(OperationKind.Move, VariableRead",
		// "Settings.keepServerlog"));
		// this is the case if gumtree.match.gt.minh" = "0" (but bad for other tests)
	}

	@Test
	public void test_t_223054() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_223054/left_GameEvent_1.2.java
		// src/test/resources/examples/t_223054/right_GameEvent_1.3.java
		File fl = new File("src/test/resources/examples/t_223054/left_GameEvent_1.2.java");
		File fr = new File("src/test/resources/examples/t_223054/right_GameEvent_1.3.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Update, "Field", "GAME_NEW_ATTACK"));
	}

	@Test
	public void test_t_223056() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_223056/left_Server_1.646.java
		// src/test/resources/examples/t_223056/right_Server_1.647.java
		File fl = new File("src/test/resources/examples/t_223056/left_Server_1.646.java");
		File fr = new File("src/test/resources/examples/t_223056/right_Server_1.647.java");

		GumtreeProperties properties = new GumtreeProperties();

		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);

		CtElement ancestor = result.commonAncestor();
		assertTrue(ancestor instanceof CtClass);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsOperation(OperationKind.Update, "Literal", "\" \""));
		assertTrue(result.containsOperation(OperationKind.Update, "Literal", "\"        \\n\""));
	}

	@Test
	public void test_t_223118() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_223118/left_TestBot_1.48.java
		// src/test/resources/examples/t_223118/right_TestBot_1.49.java
		File fl = new File("src/test/resources/examples/t_223118/left_TestBot_1.48.java");
		File fr = new File("src/test/resources/examples/t_223118/right_TestBot_1.49.java");

		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Insert, "Invocation", "refresh"));
	}

	@Test
	public void test_t_223454() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_223454/left_EntityListFile_1.17.java
		// src/test/resources/examples/t_223454/right_EntityListFile_1.18.java
		File fl = new File("src/test/resources/examples/t_223454/left_EntityListFile_1.17.java");
		File fr = new File("src/test/resources/examples/t_223454/right_EntityListFile_1.18.java");
		Diff result = diff.compare(fl, fr);

		result.getRootOperations();
		result.debugInformation();
		List<Operation> actions = result.getRootOperations();

		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Update, "ConstructorCall"));
	}

	@Test
	public void test_t_223542() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_223542/left_BoardView1_1.214.java
		// src/test/resources/examples/t_223542/right_BoardView1_1.215.java
		File fl = new File("src/test/resources/examples/t_223542/left_BoardView1_1.214.java");
		File fr = new File("src/test/resources/examples/t_223542/right_BoardView1_1.215.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Update, "FieldRead", "MOVE_VTOL_RUN"));
	}

	@Test
	public void test_t_224512() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_224512/left_Server_1.925.java
		// src/test/resources/examples/t_224512/right_Server_1.926.java
		File fl = new File("src/test/resources/examples/t_224512/left_Server_1.925.java");
		File fr = new File("src/test/resources/examples/t_224512/right_Server_1.926.java");
		Diff result = diff.compare(fl, fr);

		CtElement ancestor = result.commonAncestor();
		assertTrue(ancestor instanceof CtBinaryOperator);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsOperation(OperationKind.Insert, "BinaryOperator", "AND"));
		assertTrue(result.containsOperation(OperationKind.Move, "BinaryOperator", "AND"));
	}

	@Test
	public void test_t_224542() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_224542/left_TestBot_1.75.java
		// src/test/resources/examples/t_224542/right_TestBot_1.76.java
		File fl = new File("src/test/resources/examples/t_224542/left_TestBot_1.75.java");
		File fr = new File("src/test/resources/examples/t_224542/right_TestBot_1.76.java");

		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);

		result.debugInformation();

		CtElement ancestor = result.commonAncestor();
		assertTrue(ancestor instanceof CtInvocation);
		assertEquals("println", ((CtInvocation) ancestor).getExecutable().getSimpleName());
		assertEquals(344, ancestor.getPosition().getLine());

		List<Operation> actions = result.getAllOperations();// TODO it was root
		assertTrue(actions.size() >= 3);
		assertTrue(result.containsOperations(actions, OperationKind.Delete, "Invocation"));// , "format"
		assertTrue(result.containsOperations(actions, OperationKind.Insert, "BinaryOperator"));// , "PLUS"

		// the move can be either getEntity or getShortName
		assertTrue(result.containsOperation(OperationKind.Move, "Invocation"));
		assertEquals(344, result.changedNode(MoveOperation.class).getPosition().getLine());

	}

	@Test
	public void test_t_224766() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_224766/left_SegmentTermEnum_1.1.java
		// src/test/resources/examples/t_224766/right_SegmentTermEnum_1.2.java
		File fl = new File("src/test/resources/examples/t_224766/left_SegmentTermEnum_1.1.java");
		File fr = new File("src/test/resources/examples/t_224766/right_SegmentTermEnum_1.2.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsOperation(OperationKind.Insert, "If"));
		assertTrue(result.containsOperation(OperationKind.Move, "Invocation", "growBuffer"));
	}

	@Test
	public void test_t_224771() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_224771/left_IndexWriter_1.2.java
		// src/test/resources/examples/t_224771/right_IndexWriter_1.3.java
		File fl = new File("src/test/resources/examples/t_224771/left_IndexWriter_1.2.java");
		File fr = new File("src/test/resources/examples/t_224771/right_IndexWriter_1.3.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsOperation(OperationKind.Insert, "BinaryOperator", "OR"));
		assertTrue(result.containsOperation(OperationKind.Move, "Invocation", "hasDeletions"));
	}

	@Test
	public void test_t_224798() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_224798/left_SegmentsReader_1.4.java
		// src/test/resources/examples/t_224798/right_SegmentsReader_1.5.java
		File fl = new File("src/test/resources/examples/t_224798/left_SegmentsReader_1.4.java");
		File fr = new File("src/test/resources/examples/t_224798/right_SegmentsReader_1.5.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Update, "Invocation", "delete"));
	}

	@Test
	public void test_t_224834() throws Exception {
		// wonderful example where the text diff is impossible to comprehend
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_224834/left_TestPriorityQueue_1.2.java
		// src/test/resources/examples/t_224834/right_TestPriorityQueue_1.3.java
		File fl = new File("src/test/resources/examples/t_224834/left_TestPriorityQueue_1.2.java");
		File fr = new File("src/test/resources/examples/t_224834/right_TestPriorityQueue_1.3.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(3, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "Method", "testClear"));
	}

	@Test
	public void test_t_224863() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_224863/left_PhraseQuery_1.4.java
		// src/test/resources/examples/t_224863/right_PhraseQuery_1.5.java
		File fl = new File("src/test/resources/examples/t_224863/left_PhraseQuery_1.4.java");
		File fr = new File("src/test/resources/examples/t_224863/right_PhraseQuery_1.5.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Insert, "Assignment"));

		// the change is in the block that starts at line110
		assertEquals(110, result.changedNode().getPosition().getLine());

		// and the new element is at line 111
		assertEquals(111, actions.get(0).getNode().getPosition().getLine());
	}

	@Test
	public void test_t_224882() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_224882/left_Token_1.3.java
		// src/test/resources/examples/t_224882/right_Token_1.4.java
		File fl = new File("src/test/resources/examples/t_224882/left_Token_1.3.java");
		File fr = new File("src/test/resources/examples/t_224882/right_Token_1.4.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Update, "Literal", "\"Increment must be positive: \""));
	}

	@Test
	public void test_t_224890() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_224890/left_DateField_1.4.java
		// src/test/resources/examples/t_224890/right_DateField_1.5.java
		File fl = new File("src/test/resources/examples/t_224890/left_DateField_1.4.java");
		File fr = new File("src/test/resources/examples/t_224890/right_DateField_1.5.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "Literal", "' '"));
	}

	/**
	 * This test is ignored because we cannot replicate easily its behaviour. Its
	 * proper behaviour should be to return only one Update action as specified in
	 * the assert. However in some conditions we obtained two actions: a Delete of
	 * the method and an Insert. When studying that bug we discover that: - it's
	 * only reproducible when executing the entire test suite - it's not
	 * reproducible when using a Java debugger even without any breakpoint - it
	 * appears when changing the version of Spoon but without clear relation of what
	 * changes
	 *
	 * Given those information we think that the bug might be related with some
	 * optimization done in JVM or with the order of loading classes.
	 */
	@Ignore
	@Test
	public void test_t_225008() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225008/left_Similarity_1.9.java
		// src/test/resources/examples/t_225008/right_Similarity_1.10.java
		File fl = new File("src/test/resources/examples/t_225008/left_Similarity_1.9.java");
		File fr = new File("src/test/resources/examples/t_225008/right_Similarity_1.10.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		StringBuilder stringBuilder = new StringBuilder();
		for (Operation action : actions) {
			stringBuilder.append(action.toString());
			stringBuilder.append("\n");
		}

		assertEquals("Actions: " + stringBuilder.toString(), 1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "Modifier", "protected"));
	}

	@Test
	public void test_t_225073() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225073/left_IndexWriter_1.21.java
		// src/test/resources/examples/t_225073/right_IndexWriter_1.22.java
		File fl = new File("src/test/resources/examples/t_225073/left_IndexWriter_1.21.java");
		File fr = new File("src/test/resources/examples/t_225073/right_IndexWriter_1.22.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "NewClass"));
		// the change is in a constructor call
		assertTrue(result.changedNode() instanceof CtNewClass);
	}

	@Test
	public void test_t_286696() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_286696/left_IrmiPRODelegate_1.2.java
		// src/test/resources/examples/t_286696/right_IrmiPRODelegate_1.3.java
		File fl = new File("src/test/resources/examples/t_286696/left_IrmiPRODelegate_1.2.java");
		File fr = new File("src/test/resources/examples/t_286696/right_IrmiPRODelegate_1.3.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Update, "FieldRead", "SERVER_JRMP_PORT"));
	}

	@Test
	public void test_t_225106() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225106/left_SegmentTermDocs_1.6.java
		// src/test/resources/examples/t_225106/right_SegmentTermDocs_1.7.java
		File fl = new File("src/test/resources/examples/t_225106/left_SegmentTermDocs_1.6.java");
		File fr = new File("src/test/resources/examples/t_225106/right_SegmentTermDocs_1.7.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "BinaryOperator", "GT"));
	}

	@Test
	public void test_t_213712() throws Exception {
		// works with gumtree.match.gt.minh = 1 (and not the default 2)
		AstComparator diff = new AstComparator();
		// meld
		// src/test/resources/examples/t_213712/left_ActionAddSignalsToSignalEvent_1.2.java
		// src/test/resources/examples/t_213712/right_ActionAddSignalsToSignalEvent_1.3.java
		File fl = new File("src/test/resources/examples/t_213712/left_ActionAddSignalsToSignalEvent_1.2.java");
		File fr = new File("src/test/resources/examples/t_213712/right_ActionAddSignalsToSignalEvent_1.3.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "Field", "serialVersionUID"));
		// in Spoon 5.4 implicit blocks are made explicit
		// so we don't detect them anymore
		// assertTrue(result.containsOperation(OperationKind.Insert, "Block"));

	}

	@Test
	public void test_t_225225() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225225/left_TestSpans_1.3.java
		// src/test/resources/examples/t_225225/right_TestSpans_1.4.java
		File fl = new File("src/test/resources/examples/t_225225/left_TestSpans_1.3.java");
		File fr = new File("src/test/resources/examples/t_225225/right_TestSpans_1.4.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Delete, "LocalVariable", "buffer"));
	}

	@Test
	public void test_t_225247() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225247/left_BooleanScorer_1.10.java
		// src/test/resources/examples/t_225247/right_BooleanScorer_1.11.java
		File fl = new File("src/test/resources/examples/t_225247/left_BooleanScorer_1.10.java");
		File fr = new File("src/test/resources/examples/t_225247/right_BooleanScorer_1.11.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "BinaryOperator", "BITOR"));
	}

	@Test
	public void test_t_225262() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225262/left_FieldInfos_1.9.java
		// src/test/resources/examples/t_225262/right_FieldInfos_1.10.java
		File fl = new File("src/test/resources/examples/t_225262/left_FieldInfos_1.9.java");
		File fr = new File("src/test/resources/examples/t_225262/right_FieldInfos_1.10.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "Break"));

		// in Spoon 5.4 implicit blocks are made explicit
		// assertTrue(result.containsOperation(OperationKind.Insert, "Block"));
	}

	@Test
	@Ignore("Edit script generated is too complex. Ignoring in order to upgrade to GT3")
	public void test_t_225391() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225391/left_IndexHTML_1.4.java
		// src/test/resources/examples/t_225391/right_IndexHTML_1.5.java
		File fl = new File("src/test/resources/examples/t_225391/left_IndexHTML_1.4.java");
		File fr = new File("src/test/resources/examples/t_225391/right_IndexHTML_1.5.java");

		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 1);

		Diff result = diff.compare(fl, fr, properties);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(2, actions.size());
		assertTrue(result.containsOperations(actions, OperationKind.Delete, "Assignment"));
		assertTrue(result.containsOperations(actions, OperationKind.Insert, "Invocation")); // , "setMaxFieldLength"

		assertTrue(result.containsOperation(OperationKind.Delete, "Assignment"));
		assertTrue(result.containsOperation(OperationKind.Insert, "Invocation", "setMaxFieldLength"));
		assertTrue(result.containsOperation(OperationKind.Move, "FieldRead", "writer"));
	}

	@Test
	public void test_t_225414() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225414/left_IndexWriter_1.41.java
		// src/test/resources/examples/t_225414/right_IndexWriter_1.42.java
		File fl = new File("src/test/resources/examples/t_225414/left_IndexWriter_1.41.java");
		File fr = new File("src/test/resources/examples/t_225414/right_IndexWriter_1.42.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "Invocation", "getMessage"));
	}

	@Test
	public void test_t_225434() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225434/left_BufferedIndexInput_1.2.java
		// src/test/resources/examples/t_225434/right_BufferedIndexInput_1.3.java
		File fl = new File("src/test/resources/examples/t_225434/left_BufferedIndexInput_1.2.java");
		File fr = new File("src/test/resources/examples/t_225434/right_BufferedIndexInput_1.3.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "BinaryOperator", "EQ"));
	}

	@Test
	public void test_t_225525() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225525/left_Module_1.6.java
		// src/test/resources/examples/t_225525/right_Module_1.7.java
		File fl = new File("src/test/resources/examples/t_225525/left_Module_1.6.java");
		File fr = new File("src/test/resources/examples/t_225525/right_Module_1.7.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "Method", "getAttributes"));
	}

	@Test
	public void test_t_225724() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225724/left_ScarabRequestTool_1.36.java
		// src/test/resources/examples/t_225724/right_ScarabRequestTool_1.37.java
		File fl = new File("src/test/resources/examples/t_225724/left_ScarabRequestTool_1.36.java");
		File fr = new File("src/test/resources/examples/t_225724/right_ScarabRequestTool_1.37.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "Invocation", "error"));
	}

	@Test
	public void test_t_225893() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_225893/left_RQueryUser_1.1.java
		// src/test/resources/examples/t_225893/right_RQueryUser_1.2.java
		File fl = new File("src/test/resources/examples/t_225893/left_RQueryUser_1.1.java");
		File fr = new File("src/test/resources/examples/t_225893/right_RQueryUser_1.2.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "Method", "delete"));
	}

	@Test
	public void test_t_226145() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_226145/left_ScarabRequestTool_1.90.java
		// src/test/resources/examples/t_226145/right_ScarabRequestTool_1.91.java
		File fl = new File("src/test/resources/examples/t_226145/left_ScarabRequestTool_1.90.java");
		File fr = new File("src/test/resources/examples/t_226145/right_ScarabRequestTool_1.91.java");
		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "Method", "getIssueByUniqueId"));
	}

	@Test
	public void test_t_226330() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_226330/left_ActivityRule_1.4.java
		// src/test/resources/examples/t_226330/right_ActivityRule_1.5.java
		File fl = new File("src/test/resources/examples/t_226330/left_ActivityRule_1.4.java");
		File fr = new File("src/test/resources/examples/t_226330/right_ActivityRule_1.5.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "TypeAccess", "DBImport.STATE_DB_INSERTION"));
	}

	@Test
	public void test_t_226480() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_226480/left_ScarabRequestTool_1.113.java
		// src/test/resources/examples/t_226480/right_ScarabRequestTool_1.114.java
		File fl = new File("src/test/resources/examples/t_226480/left_ScarabRequestTool_1.113.java");
		File fr = new File("src/test/resources/examples/t_226480/right_ScarabRequestTool_1.114.java");
		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);
		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "Invocation", "debug"));
	}

	@Test
	public void test_t_226555() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_226555/left_Attachment_1.24.java
		// src/test/resources/examples/t_226555/right_Attachment_1.25.java
		File fl = new File("src/test/resources/examples/t_226555/left_Attachment_1.24.java");
		File fr = new File("src/test/resources/examples/t_226555/right_Attachment_1.25.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(2, actions.size());
		// root actions
		// Issue #102
		// assertTrue(result.containsOperation(OperationKind.Update, "Invocation",
		// "lastIndexOf"));
		// low level actions
		assertTrue(
				result.containsOperations(result.getRootOperations(), OperationKind.Delete, "FieldRead", "separator"));
		assertTrue(result.containsOperations(result.getRootOperations(), OperationKind.Insert, "Literal", "'/'"));
	}

	@Test
	public void test_t_226622() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_226622/left_AttributeValue_1.49.java
		// src/test/resources/examples/t_226622/right_AttributeValue_1.50.java
		File fl = new File("src/test/resources/examples/t_226622/left_AttributeValue_1.49.java");
		File fr = new File("src/test/resources/examples/t_226622/right_AttributeValue_1.50.java");
		Diff result = diff.compare(fl, fr);

		result.getRootOperations();
		result.debugInformation();

		// no assert on number of actions because a move migt be detected (TODO?)
		assertTrue(result.containsOperation(OperationKind.Insert, "BinaryOperator", "AND"));
	}

	@Test
	public void test_t_226685() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_226685/left_ResetCacheValve_1.1.java
		// src/test/resources/examples/t_226685/right_ResetCacheValve_1.2.java
		File fl = new File("src/test/resources/examples/t_226685/left_ResetCacheValve_1.1.java");
		File fr = new File("src/test/resources/examples/t_226685/right_ResetCacheValve_1.2.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Delete, "Invocation", "println"));
	}

	@Test
	public void test_t_226926() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_226926/left_ScarabUserManager_1.4.java
		// src/test/resources/examples/t_226926/right_ScarabUserManager_1.5.java
		File fl = new File("src/test/resources/examples/t_226926/left_ScarabUserManager_1.4.java");
		File fr = new File("src/test/resources/examples/t_226926/right_ScarabUserManager_1.5.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "Modifier", "public"));
	}

	@Test
	public void test_t_226963() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_226963/left_Issue_1.140.java
		// src/test/resources/examples/t_226963/right_Issue_1.141.java
		File fl = new File("src/test/resources/examples/t_226963/left_Issue_1.140.java");
		File fr = new File("src/test/resources/examples/t_226963/right_Issue_1.141.java");
		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);
		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "Invocation", "addAscendingOrderByColumn"));
	}

	@Test
	public void test_t_227005() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_227005/left_AttributeValue_1.56.java
		// src/test/resources/examples/t_227005/right_AttributeValue_1.57.java
		File fl = new File("src/test/resources/examples/t_227005/left_AttributeValue_1.56.java");
		File fr = new File("src/test/resources/examples/t_227005/right_AttributeValue_1.57.java");
		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);
		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(2, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "BinaryOperator", "AND"));
		assertTrue(result.containsOperation(OperationKind.Move, "BinaryOperator", "AND"));
	}

	@Test
	public void test_t_227130() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_227130/left_Transaction_1.37.java
		// src/test/resources/examples/t_227130/right_Transaction_1.38.java
		File fl = new File("src/test/resources/examples/t_227130/left_Transaction_1.37.java");
		File fr = new File("src/test/resources/examples/t_227130/right_Transaction_1.38.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "Method", "create"));
	}

	@Test
	public void test_t_227368() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_227368/left_IssueTemplateInfo_1.12.java
		// src/test/resources/examples/t_227368/right_IssueTemplateInfo_1.13.java
		File fl = new File("src/test/resources/examples/t_227368/left_IssueTemplateInfo_1.12.java");
		File fr = new File("src/test/resources/examples/t_227368/right_IssueTemplateInfo_1.13.java");
		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 1);

		Diff result = diff.compare(fl, fr, properties);
		List<Operation> actions = result.getAllOperations();
		result.debugInformation();
		assertEquals(1, actions.size());

		assertTrue(result.containsOperation(OperationKind.Insert, "Literal", "null"));

		// GT 3: the move is not detected
		//// one parameter is moved to another argument
		// assertTrue(result.containsOperation(OperationKind.Move, "Invocation"));
	}

	@Test
	public void test_t_227811() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_227811/left_RModuleIssueType_1.24.java
		// src/test/resources/examples/t_227811/right_RModuleIssueType_1.25.java
		File fl = new File("src/test/resources/examples/t_227811/left_RModuleIssueType_1.24.java");
		File fr = new File("src/test/resources/examples/t_227811/right_RModuleIssueType_1.25.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "Invocation", "setDisplayDescription"));
	}

	@Test
	public void test_t_227985() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_227985/left_IssueSearch_1.65.java
		// src/test/resources/examples/t_227985/right_IssueSearch_1.66.java
		File fl = new File("src/test/resources/examples/t_227985/left_IssueSearch_1.65.java");
		File fr = new File("src/test/resources/examples/t_227985/right_IssueSearch_1.66.java");
		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);
		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "Assignment"));
	}

	@Test
	public void test_t_228064() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_228064/left_ModuleManager_1.21.java
		// src/test/resources/examples/t_228064/right_ModuleManager_1.22.java
		File fl = new File("src/test/resources/examples/t_228064/left_ModuleManager_1.21.java");
		File fr = new File("src/test/resources/examples/t_228064/right_ModuleManager_1.22.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "Modifier", "public"));
	}

	@Test
	public void test_t_1205753_exception_not_attached_node() throws Exception {
		AstComparator diff = new AstComparator();
		// the problem is: the tree node corresponding to an exception does not have
		// attached the corresponding GT node
		File fl = new File("src/test/resources/examples/t_1205753/1205753_EmbedPooledConnection_0_s.java");
		File fr = new File("src/test/resources/examples/t_1205753/1205753_EmbedPooledConnection_0_t.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		Operation operation = actions.get(0);
		assertNotNull(operation.getSrcNode());
		Object relatedSpoonObject = operation.getAction().getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(relatedSpoonObject);
		assertEquals(operation.getSrcNode(), relatedSpoonObject);
	}

	@Test
	public void test_t_228325() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_228325/left_ForgotPassword_1.10.java
		// src/test/resources/examples/t_228325/right_ForgotPassword_1.11.java
		File fl = new File("src/test/resources/examples/t_228325/left_ForgotPassword_1.10.java");
		File fr = new File("src/test/resources/examples/t_228325/right_ForgotPassword_1.11.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "Literal", "\"ForgotPassword.vm\""));
	}

	@Test
	public void test_t_228643() throws Exception {
		// works only if AbstractBottomUpMatcher.SIZE_THRESHOLD >= 7
		// AbstractBottomUpMatcher.SIZE_THRESHOLD = 10;
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_228643/left_ScopePeer_1.3.java
		// src/test/resources/examples/t_228643/right_ScopePeer_1.4.java
		File fl = new File("src/test/resources/examples/t_228643/left_ScopePeer_1.3.java");
		File fr = new File("src/test/resources/examples/t_228643/right_ScopePeer_1.4.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(
				result.containsOperation(OperationKind.Update, "ConstructorCall", "org.apache.torque.util.Criteria()"));
	}

	@Test
	public void test_issue31() throws Exception {
		// https://github.com/SpoonLabs/gumtree-spoon-ast-diff/issues/31;
		// the cause of this bug is the value of gumtree.match.bu.sim set in
		// AstComparator
		// with gumtree.match.bu.sim=0.4 (the previous value), the block of the whole
		// method (starting line 408) was not mapped, and this created a lot of spurious
		// moves
		// with gumtree.match.bu.sim=0.6 (the new default value in the commit to fix the
		// bug), the block of the whole method is mapped, and the diff becomes perfect
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/issue31/original.java
		// src/test/resources/examples/issue31/patched.java
		File fl = new File("src/test/resources/examples/issue31/original.java");
		File fr = new File("src/test/resources/examples/issue31/patched.java");
		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);
		List<Operation> rootActions = result.getRootOperations();
		// result.debugInformation();
		System.out.println("root: " + result.getRootOperations().size());
		for (Operation o : result.getRootOperations()) {
			System.out.println(o.getClass().getSimpleName() + " " + o.getSrcNode().getClass().getSimpleName() + " "
					+ o.getSrcNode().getPosition().getLine());
		}
		System.out.println("all: " + result.getAllOperations().size());
		assertEquals(2, rootActions.size());
		assertTrue(result.containsOperation(OperationKind.Delete, "If", "if"));
		assertTrue(result.containsOperation(OperationKind.Move, "If")); // the else if moved one level up
	}

	public void test_chart18() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/chart18/DefaultKeyedValues2D.java");
		File fr = new File("src/test/resources/examples/chart18/new_DefaultKeyedValues2D.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(5, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "LocalVariable", "index"));
		assertTrue(result.containsOperation(OperationKind.Insert, "If", "if"));
	}

	@Test
	public void test_issue59() throws Exception {
		CtClass c1 = Launcher.parseClass(" class foo{public static void main(String[] args){} }");

		CtClass c2 = Launcher.parseClass(" class foo{public static void main(String[] args) throws Exception{} }");

		AstComparator diff = new AstComparator();
		Diff result = diff.compare(c1, c2);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();

		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "THROWN_TYPES"));
	}

	@Test
	public void test_issue201812() throws Exception {
		// https://github.com/GumTreeDiff/gumtree/issues/120
		CtClass c1 = Launcher.parseClass(" class BehaviorCall implements Call{\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "\n" + " enqueue(new Callback<T>() {\n"
				+ "  @Override public void onResponse(Response<T> response) {\n" + "     responseRef.set(response);\n"
				+ "     latch.countDown();\n" + "   }\n" + "}\n" + ")\n" + "\n" + "}");

		CtClass c2 = Launcher.parseClass("class BehaviorCall implements Call {\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "enqueue(new Callback() {\n"
				+ "@Override public void onResponse(Call call, Response<T> response) {\n"
				+ "responseRef.set(response);\n" + "latch.countDown();\n" + "}\n" + "}\n" + ")\n" + "}");

		AstComparator diff = new AstComparator();
		Diff result = diff.compare(c1, c2);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();

		assertEquals(2, actions.size());
		assertTrue(result.containsOperation(OperationKind.Delete, "TYPE_ARGUMENT", "T"));
		assertTrue(result.containsOperation(OperationKind.Insert, "Parameter", "call"));
	}

	@Test
	public void test_vs_6b994_AuthorizationHelper() throws Exception {
		AstComparator diff = new AstComparator();
		// meld src/test/resources/examples/t_212496/left_CoreHelperImpl_1.29.java
		// src/test/resources/examples/t_212496/right_CoreHelperImpl_1.30.java
		File fl = new File("src/test/resources/examples/vs/06b994/AuthorizationHelper/AuthorizationHelper_s.java");
		File fr = new File("src/test/resources/examples/vs/06b994/AuthorizationHelper/AuthorizationHelper_t.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		// result.debugInformation();
		assertNotNull(actions);
		assertTrue(actions.size() > 0);
		System.out.println("Actions " + actions);
		// assertEquals(actions.size(), 1);
		assertTrue(result.containsOperation(OperationKind.Delete, "LocalVariable"));
		assertTrue(result.containsOperation(OperationKind.Delete, "Invocation"));
	}

	@Test
	public void test_vs_6b994_UtilityService() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/vs/06b994/UtilityService/UtilityService_s.java");
		File fr = new File("src/test/resources/examples/vs/06b994/UtilityService/UtilityService_t.java");
		Diff result = diff.compare(fl, fr);

		List<Operation> actions = result.getRootOperations();
		assertNotNull(actions);
		assertTrue(actions.size() > 0);

	}

	@Test
	public void test_vs_6b994_VerificationHost() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/vs/06b994/VerificationHost/VerificationHost_s.java");
		File fr = new File("src/test/resources/examples/vs/06b994/VerificationHost/VerificationHost_t.java");
		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);

		List<Operation> actions = result.getRootOperations();
		assertNotNull(actions);
		assertTrue(actions.size() > 0);
		assertEquals(3, actions.size());
		assertTrue(result.containsOperation(OperationKind.Delete, "If"));
		assertTrue(result.containsOperation(OperationKind.Delete, "LocalVariable"));
	}

	@Test
	public void test_vs_6b994_TestUtilityService() throws Exception {
		AstComparator diff = new AstComparator();

		File fl = new File("src/test/resources/examples/vs/06b994/TestUtilityService/TestUtilityService_s.java");
		File fr = new File("src/test/resources/examples/vs/06b994/TestUtilityService/TestUtilityService_t.java");
		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);
		List<Operation> actions = result.getRootOperations();
		assertNotNull(actions);
		assertTrue(actions.size() > 0);
		System.out.println("Actions " + actions);
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Delete, "Method"));
	}

	@Test
	public void testNoLabel1() throws Exception {
		System.setProperty("nolabel", "true");
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/t_nolabel1/left_CmiContext_1.2.java");
		File fr = new File("src/test/resources/examples/t_nolabel1/right_CmiContext_1.2.java");
		Diff result = diff.compare(fl, fr);
		List<Operation> actions = result.getRootOperations();
		System.out.println("action");
		assertEquals(0, actions.size());
		System.setProperty("nolabel", "false");
	}

	@Test
	public void testNoLabel2() throws Exception {
		System.setProperty("nolabel", "true");
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/t_nolabel2/left_CmiContext_1.2.java");
		File fr = new File("src/test/resources/examples/t_nolabel2/right_CmiContext_1.2.java");
		Diff result = diff.compare(fl, fr);
		List<Operation> actions = result.getRootOperations();
		System.out.println("action " + actions);
		assertEquals(0, actions.size());
		System.setProperty("nolabel", "false");
	}

	@Test
	public void testNoLabel3() throws Exception {
		System.setProperty("nolabel", "true");
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/t_nolabel3/left_CmiContext_1.2.java");
		File fr = new File("src/test/resources/examples/t_nolabel3/right_CmiContext_1.2.java");
		Diff result = diff.compare(fl, fr);
		List<Operation> actions = result.getRootOperations();
		System.out.println("action " + actions);
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "Invocation"));
		System.setProperty("nolabel", "false");
	}

	@Test
	public void testInsertStaticFromMethodHead() throws Exception {
		AstComparator diff = new AstComparator();
		Diff result = diff.compare(
				"public class Calculator {\n" + "    public int add(int a, int b){\n" + "        return a + b;\n"
						+ "    }\n" + "}",
				"public class Calculator {\n" + "    public static int add(int a, int b){\n" + "        return a + b;\n"
						+ "    }\n" + "}");

		assertEquals(1, result.getRootOperations().size());

	}

	@Test
	public void testFileName() throws Exception {
		AstComparator diff = new AstComparator();
		String left = "public class Calculator {\n" + "    public int add(int a, int b){\n" + "        return a + b;\n"
				+ "    }\n" + "}";
		String right = "public class Calculator {\n" + "    public int add(int a, int b){\n"
				+ "    a = a + b;    return a + b;\n" + "    }\n" + "}";
		Diff result = diff.compare(left, right);

		assertEquals(1, result.getRootOperations().size());
		// by default, name is "test"
		assertEquals("test", result.getRootOperations().get(0).getSrcNode().getPosition().getFile().getName());

		// let's put other names
		result = diff.compare(left, right, "mleft.java", "mright.java");

		assertEquals(1, result.getRootOperations().size());

		assertTrue(result.getRootOperations().get(0) instanceof InsertOperation);

		assertEquals("mright.java", result.getRootOperations().get(0).getSrcNode().getPosition().getFile().getName());

		InsertOperation ins = (InsertOperation) result.getRootOperations().get(0);

		assertEquals("mleft.java", ins.getParent().getPosition().getFile().getName());

	}

	@Test
	public void testVarargs() throws Exception {
		// https://github.com/GumTreeDiff/gumtree/issues/120
		CtClass c1 = Launcher.parseClass(" class BehaviorCall {\n" + "   void foo(String s)}\n" + "}\n" + "\n" + "}");

		CtClass c2 = Launcher
				.parseClass(" class BehaviorCall {\n" + "   void foo(String... s)}\n" + "}\n" + "\n" + "}");

		AstComparator diff = new AstComparator();
		Diff result = diff.compare(c1, c2);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();

		assertEquals(1, actions.size());
		// the type is now an array
		assertTrue(result.containsOperations(OperationKind.Update, "VARIABLE_TYPE(CtTypeReferenceImpl)",
				"java.lang.String", "java.lang.String[]"));
	}

	@Test
	public void testModifEmpty() throws Exception {

		CtClass c1a = Launcher.parseClass(" class BehaviorCall implements Call{\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "\n" + " enqueue(new Callback<T>() {\n"
				+ "  @Override public void onResponse(Response<T> response) {\n" + "     responseRef.set(response);\n"
				+ "     latch.countDown();\n" + "   }\n" + "}\n" + ")\n" + "\n" + "}");

		CtClass c2a = Launcher.parseClass("class BehaviorCall implements Call {\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "enqueue(new Callback() {\n"
				// Here the difference
				+ "@Override public void onResponse(Call call, Response<T> response) {\n"
				+ "responseRef.set(response);\n" + "latch.countDown();\n" + "}\n" + "}\n" + ")\n" + "}");

		AstComparator diff = new AstComparator();
		Diff resulta = diff.compare(c1a, c2a);

		List<Operation> actions = resulta.getRootOperations();
		resulta.debugInformation();

		assertEquals(2, actions.size());
		assertTrue(resulta.containsOperation(OperationKind.Delete, "TYPE_ARGUMENT", "T"));
		assertTrue(resulta.containsOperation(OperationKind.Insert, "Parameter", "call"));

		DiffImpl idiff = (DiffImpl) resulta;

		for (Mapping map : idiff.getMappingsComp()) {
			// if
			// ((map.first.toPrettyString(idiff.getContext()).startsWith(NodeCreator.MODIFIERS)))
			// {
			// assertFalse(map.first.getChildren().isEmpty());
			// assertFalse(map.second.getChildren().isEmpty());
			// }
		}

	}

	@Test
	public void testExtends() throws Exception {
		CtClass c1a = Launcher.parseClass("class Main extends SuperClass1 { }");
		CtClass c2a = Launcher.parseClass("class Main extends SuperClass2 { }");

		AstComparator diff = new AstComparator();
		Diff result = diff.compare(c1a, c2a);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();

		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "SUPER_TYPE", "SuperClass1"));
	}

	@Test
	public void testExtendsGenerics1() throws Exception {
		CtClass c1a = Launcher.parseClass("class Main extends SuperClass<One> { }");
		CtClass c2a = Launcher.parseClass("class Main extends SuperClass<Two> { }");

		AstComparator diff = new AstComparator();
		Diff result = diff.compare(c1a, c2a);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();

		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "TYPE_ARGUMENT", "One"));
	}

	@Test
	public void testExtendsGenerics2() throws Exception {
		CtClass c1a = Launcher.parseClass("class Main extends SuperClass { }");
		CtClass c2a = Launcher.parseClass("class Main extends SuperClass<One> { }");

		AstComparator diff = new AstComparator();
		Diff result = diff.compare(c1a, c2a);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();

		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Insert, "TYPE_ARGUMENT", "One"));
	}

	@Test
	public void testD4JLang57() throws Exception {
		AstComparator diff = new AstComparator();
		// meld
		// src/test/resources/examples/d4j/Lang_57/LocaleUtils/Lang_57_LocaleUtils_s.java
		// src/test/resources/examples/d4j/Lang_57/LocaleUtils/Lang_57_LocaleUtils_t.java
		File fl = new File("src/test/resources/examples/d4j/Lang_57/LocaleUtils/Lang_57_LocaleUtils_s.java");
		File fr = new File("src/test/resources/examples/d4j/Lang_57/LocaleUtils/Lang_57_LocaleUtils_t.java");

		Diff result = diff.compare(fl, fr);
		result.debugInformation();
		List<Operation> actions = result.getAllOperations();
		assertEquals(2, actions.size());
		assertTrue(result.containsOperation(OperationKind.Delete, "FieldRead", "cAvailableLocaleSet"));
		assertTrue(result.containsOperation(OperationKind.Insert, "Invocation", "availableLocaleList"));

	}

	@Test
	public void testD4JMath34() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/d4j/Math_34/ListPopulation/Math_34_ListPopulation_s.java");
		File fr = new File("src/test/resources/examples/d4j/Math_34/ListPopulation/Math_34_ListPopulation_t.java");
		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff result = diff.compare(fl, fr, properties);
		List<Operation> actions = result.getAllOperations();
		assertEquals(5, actions.size());

	}

	@Test
	public void testDiff1Comment() throws Exception {
		File s = new File("src/test/resources/examples/diffcomment1/1205753_EmbedPooledConnection_0_s.java");
		File t = new File("src/test/resources/examples/diffcomment1/1205753_EmbedPooledConnection_0_t.java");
		boolean includeComments = true;
		AstComparator r = new AstComparator(includeComments);
		Diff diffOut = r.compare(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
		Operation op = diffOut.getRootOperations().get(0);
		Assert.assertTrue(op.getSrcNode().getComments().size() > 0);

		List<Operation> allop = diffOut.getAllOperations();
		boolean hasComment = false;
		for (Operation operation : allop) {
			hasComment = hasComment || (operation.getSrcNode() instanceof CtComment);
		}
		assertTrue(hasComment);

	}

	@Test
	public void testDiff2Comment() throws Exception {
		File s = new File("src/test/resources/examples/diffcomment2/1205753_EmbedPooledConnection_0_s.java");
		File t = new File("src/test/resources/examples/diffcomment2/1205753_EmbedPooledConnection_0_t.java");
		boolean includeComments = true;
		AstComparator r = new AstComparator(includeComments);

		Diff diffOut = r.compare(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
		Operation op = diffOut.getRootOperations().get(0);
		// Assert.assertTrue(op.getSrcNode().getComments().size() > 0);

		List<Operation> allop = diffOut.getAllOperations();
		boolean hasComment = false;
		for (Operation operation : allop) {
			hasComment = hasComment || (operation.getSrcNode() instanceof CtComment);
		}
		assertTrue(hasComment);

	}

	@Test
	public void testDiff3Comment() throws Exception {
		File s = new File("src/test/resources/examples/diffcomment3/RectangularCholeskyDecomposition_s.java");
		File t = new File("src/test/resources/examples/diffcomment3/RectangularCholeskyDecomposition_t.java");
		boolean includeComments = true;
		AstComparator r = new AstComparator(includeComments);

		Diff diffOut = r.compare(s, t);
		System.out.println("Output: " + diffOut);
		Assert.assertEquals(1, diffOut.getRootOperations().size());
		Operation op = diffOut.getRootOperations().get(0);
		Assert.assertTrue(op.getSrcNode().getComments().size() > 0);

		assertFalse(op.getSrcNode() instanceof CtComment);

		List<Operation> allop = diffOut.getAllOperations();
		boolean hasComment = false;
		for (Operation operation : allop) {
			if ((operation.getSrcNode() instanceof CtComment)) {
				hasComment = true;
				System.out.println(operation.getSrcNode());
			}
		}
		assertTrue(hasComment);

	}

	@Test
	public void testReplaceMovesFromRoot1() {
		String c1 = "" + "class X {" + "public void foo0() {" + " int z = 0;" + " int x = 0;" + " int y = 0;" + "}"
				+ "};";

		String c2 = "" + "class X {" + "public void foo0() {" + " int x = 0;" + " int y = 0;" + " int z = 0;" + "}"
				+ "};";

		AstComparator diff = new AstComparator();
		GumtreeProperties properties = new GumtreeProperties();
		properties = new GumtreeProperties();

		properties.tryConfigure(ConfigurationOptions.st_minprio, 0);

		Diff editScript = diff.compare(c1, c2, properties);

		assertEquals(1, editScript.getRootOperations().size());

		assertEquals(1, editScript.getAllOperations().size());

		Optional<Operation> moveOpt = editScript.getAllOperations().stream().filter(e -> e instanceof MoveOperation)
				.findAny();
		assertTrue(moveOpt.isPresent());

		List<Operation> newOps = ActionClassifier.replaceMoveFromRoots(editScript);

		Optional<Operation> moveOptN = newOps.stream().filter(e -> e instanceof MoveOperation).findAny();
		assertFalse(moveOptN.isPresent());

		assertEquals(2, newOps.size());

		Optional<Operation> insertOpt = newOps.stream().filter(e -> e instanceof InsertOperation).findAny();
		assertTrue(insertOpt.isPresent());
		Optional<Operation> deleteOpt = newOps.stream().filter(e -> e instanceof DeleteOperation).findAny();
		assertTrue(deleteOpt.isPresent());
		// failing: commented for Gt3
		// Move moveAction = (Move) (moveOpt.get().getAction());
		// assertTrue(editScript.getMappingsComp().isDstMapped(moveAction.getParent()));

		// assertFalse(editScript.getMappingsComp().isSrcMapped(moveAction.getParent()));

		// Same object
		assertTrue(deleteOpt.get().getNode() == moveOpt.get().getNode());
		assertTrue(deleteOpt.get().getAction().getNode() == moveOpt.get().getAction().getNode());
		// Same content
		assertEquals(deleteOpt.get().getNode().toString(), moveOpt.get().getNode().toString());

		// Different object
		assertTrue(insertOpt.get().getNode() != moveOpt.get().getNode());
		assertTrue(insertOpt.get().getAction().getNode() != moveOpt.get().getAction().getNode());
		assertEquals(insertOpt.get().getNode().toString(), moveOpt.get().getNode().toString());

	}

	@Test
	public void testReplaceMovesFromRoot2() {
		String c1 = "" + "class X {" + "public void foo0() {" + " int z = 0;" + " int x = 0;" + " int y = 0;" + "}"
				+ "};";

		String c2 = "" + "class X {" + "public void foo0() {" + " int x = 0;" + " int y = 0;" + "if(y>0){ int z = 0;}"
				+ "}" + "};";

		AstComparator diff = new AstComparator();
		Diff editScript = diff.compare(c1, c2);

		assertEquals(2, editScript.getRootOperations().size());

		assertEquals(1, editScript.getAllOperations().stream().filter(e -> e instanceof MoveOperation).count());

		Optional<Operation> moveOpt = editScript.getAllOperations().stream().filter(e -> e instanceof MoveOperation)
				.findAny();
		assertTrue(moveOpt.isPresent());

		List<Operation> newOps = ActionClassifier.replaceMoveFromRoots(editScript);

		Optional<Operation> moveOptN = newOps.stream().filter(e -> e instanceof MoveOperation).findAny();
		assertFalse(moveOptN.isPresent());

		Move moveAction = (Move) (moveOpt.get().getAction());
		assertFalse(editScript.getMappingsComp().isDstMapped(moveAction.getParent()));

		assertFalse(editScript.getMappingsComp().isSrcMapped(moveAction.getParent()));

		Optional<Operation> insertOpt = newOps.stream()
				.filter(e -> e instanceof InsertOperation && e.getNode() instanceof CtLocalVariable).findAny();
		// The insert must not exist
		assertFalse(insertOpt.isPresent());
		Optional<Operation> deleteOpt = newOps.stream()
				.filter(e -> e instanceof DeleteOperation && e.getNode() instanceof CtLocalVariable).findAny();
		assertTrue(deleteOpt.isPresent());

		assertEquals(2, newOps.size());

		Optional<Operation> insertIfOpt = newOps.stream()
				.filter(e -> e instanceof InsertOperation && e.getNode() instanceof CtIf).findAny();
		assertTrue(insertIfOpt.isPresent());

		// Same object
		assertTrue(deleteOpt.get().getNode() == moveOpt.get().getNode());
		assertTrue(deleteOpt.get().getAction().getNode() == moveOpt.get().getAction().getNode());
		// Same content
		assertEquals(deleteOpt.get().getNode().toString(), moveOpt.get().getNode().toString());

	}

	@Test
	public void testReplaceMovesFromAll2() {
		String c1 = "" + "class X {" + "public void foo0() {" + " int z = 0;" + " int x = 0;" + " int y = 0;" + "}"
				+ "};";

		String c2 = "" + "class X {" + "public void foo0() {" + " int x = 0;" + " int y = 0;" + "if(y>0){ int z = 0;}"
				+ "}" + "};";

		AstComparator diff = new AstComparator();
		Diff editScript = diff.compare(c1, c2);

		assertEquals(2, editScript.getRootOperations().size());

		assertEquals(1, editScript.getAllOperations().stream().filter(e -> e instanceof MoveOperation).count());

		Optional<Operation> moveOpt = editScript.getAllOperations().stream().filter(e -> e instanceof MoveOperation)
				.findAny();
		assertTrue(moveOpt.isPresent());

		List<Operation> newOps = ActionClassifier.replaceMoveFromAll(editScript);

		Optional<Operation> moveOptN = newOps.stream().filter(e -> e instanceof MoveOperation).findAny();
		assertFalse(moveOptN.isPresent());

		Move moveAction = (Move) (moveOpt.get().getAction());
		assertFalse(editScript.getMappingsComp().isDstMapped(moveAction.getParent()));

		assertFalse(editScript.getMappingsComp().isSrcMapped(moveAction.getParent()));

		Optional<Operation> insertOpt = newOps.stream()
				.filter(e -> e instanceof InsertOperation && e.getNode() instanceof CtLocalVariable).findAny();
		// The insert must exist in all
		assertTrue(insertOpt.isPresent());
		Optional<Operation> deleteOpt = newOps.stream()
				.filter(e -> e instanceof DeleteOperation && e.getNode() instanceof CtLocalVariable).findAny();
		assertTrue(deleteOpt.isPresent());

		Optional<Operation> insertIfOpt = newOps.stream()
				.filter(e -> e instanceof InsertOperation && e.getNode() instanceof CtIf).findAny();
		assertTrue(insertIfOpt.isPresent());

		// Same object
		assertTrue(deleteOpt.get().getNode() == moveOpt.get().getNode());
		assertTrue(deleteOpt.get().getAction().getNode() == moveOpt.get().getAction().getNode());
		// Same content
		assertEquals(deleteOpt.get().getNode().toString(), moveOpt.get().getNode().toString());

	}

	@Test
	public void thisAndSuperShouldResultInAnASTDiff() {
		// arrange
		String c3 = "class X { public  X() { this(); } }";
		String c4 = "class X { public X() { super();} }";
		AstComparator comparator = new AstComparator();

		// act
		Diff diff = comparator.compare(c3, c4);

		// assert
		assertTrue(diff.containsOperations(OperationKind.Update, "Invocation", "this", "super"));
	}
}
