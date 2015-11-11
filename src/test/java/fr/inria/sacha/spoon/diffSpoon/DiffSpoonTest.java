package fr.inria.sacha.spoon.diffSpoon;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import spoon.Launcher;
import spoon.compiler.SpoonCompiler;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.support.compiler.jdt.JDTSnippetCompiler;

import com.github.gumtreediff.actions.model.Action;

/**
 * Test Spoon Diff 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class DiffSpoonTest {

	
	@Test
	public void testgetCtType() throws Exception {
		String c1 = "package spoon1.test; " 
	+ "" + "class X {" + "public void foo0() {" + " int x = 0;"
				+ "}" + "}";
		DiffSpoon diff = new DiffSpoon(true);
		CtType<?> t1 = diff.getCtType(c1);
		assertTrue(t1 != null);
	
	}
	
	@Test
	public void testAnalyzeStringString() {
		String c1 = "" + "class X {" + "public void foo0() {" + " int x = 0;"
				+ "}" + "};";
		
		String c2 = "" + "class X {" + "public void foo1() {" + " int x = 0;"
				+ "}" + "};";
		
		
		DiffSpoon diff = new DiffSpoon(true);
		CtDiff editScript = diff.compare(c1, c2);
		assertTrue(editScript.rootActions.size() == 1);
	}


	@Test
	public void exampleInsertAndUpdate() throws Exception{
		
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/test1/TypeHandler1.java");
		File fr = new File("src/test/resources/examples/test1/TypeHandler2.java");
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 2);

		System.out.println(actions);
		assertTrue(diff.containsAction(actions, "INS", "Invocation"));
		assertTrue(diff.containsAction(actions, "UPD", "FieldRead"));
		
		assertFalse(diff.containsAction(actions, "DEL", "Invocation"));
		assertFalse(diff.containsAction(actions, "UPD", "Invocation"));
		
	}
	
	
	@Test
	public void exampleSingleUpdate() throws Exception{
		
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/test2/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test2/CommandLine2.java");
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "UPD", "Literal"/*"PAR-Literal"*/));
	
	}
	
	@Test
	public void exampleRemoveMethod() throws Exception{
		
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/test3/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test3/CommandLine2.java");
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "DEL", "Method"));
	}
	
	
	@Test
	public void exampleInsert() throws Exception{
		
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/test4/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test4/CommandLine2.java");
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		System.out.println(actions);
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "INS", "Method","resolveOptionNew"));
	}
	
	@Test
	public void testMain() throws Exception{
		
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/test4/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test4/CommandLine2.java");
	
		DiffSpoon.main(new String []{fl.getAbsolutePath(), fr.getAbsolutePath()});
	}
	@Test
	public void testContent() throws Exception{
		File fl = new File("src/test/resources/examples/test4/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test4/CommandLine2.java");
		DiffSpoon diff = new DiffSpoon(true);
		CtType ctl = diff.getSpoonType(diff.readFile(fl));
		assertNotNull(ctl);
	}
	
	
	@Test
	public void testJDTBasedSpoonCompiler(){
		
	/*	String c1 = "package spoon1.test; import org.junit.Test; " + "class X {" + "public void foo0() {" + " int x = 0;"
				+ "}" + "};";*/
		
		
		String content1 = "package spoon1.test;  " 
				+ "" + "class X {" + "public void foo0() {" + " int x = 0;"
							+ "}" + "}";
		
		Factory factory = new Launcher().createFactory();
		
		SpoonCompiler compiler = new JDTSnippetCompiler(factory, content1);//new JDTBasedSpoonCompiler(factory);
	//	compiler.addInputSource(new VirtualFile(c1,""));
		compiler.build();
		CtClass<?> clazz1 = (CtClass<?>) factory.Type().getAll().get(0);
	//	factory.Package().getAllRoots().clear();
		
		Assert.assertNotNull(clazz1);
	}
	
	@Test
	public void test5() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/test5/left_LmiInitialContext_1.5.java");
		File fr = new File("src/test/resources/examples/test5/right_LmiInitialContext_1.6.java");
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		System.out.println(actions);
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "UPD", "BinaryOperator","AND"));
	}
	
	@Test
	public void test6() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/test6/A.java");
		File fr = new File("src/test/resources/examples/test6/B.java");
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		System.out.println(actions);
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "DEL", "Parameter","i"));
	}

	@Test
	public void test7() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/test7/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/test7/right_QuickNotepad_1.14.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(actions);
		assertEquals(actions.size(), 2);
		assertTrue(diff.containsAction(actions, "DEL", "Invocation", "addKeyListener"));
		assertTrue(diff.containsAction(actions, "DEL", "Class","KeyHandler"));
	}

	@Test
	public void test_t_286700() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld src/test/resources/examples/t_286700/left_CmiContext_1.2.java src/test/resources/examples/t_286700/right_CmiContext_1.3.java
		File fl = new File("src/test/resources/examples/t_286700/left_CmiContext_1.2.java");
		File fr = new File("src/test/resources/examples/t_286700/right_CmiContext_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		diff.printActions(actions);
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "INS", "Method", "getObjectPort"));
		
	}

	@Test
	public void test_t_202564() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_202564/left_PropPanelModelElement_1.9.java src/test/resources/examples/t_202564/right_PropPanelModelElement_1.10.java
		File fl = new File("src/test/resources/examples/t_202564/left_PropPanelModelElement_1.9.java");
		File fr = new File("src/test/resources/examples/t_202564/right_PropPanelModelElement_1.10.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		diff.printActions(actions);
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Insert", "Field", "_assocEndRoleIcon"));
	}

	@Test
	public void test_t_204225() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_204225/left_UMLModelElementStereotypeComboBoxModel_1.3.java src/test/resources/examples/t_204225/right_UMLModelElementStereotypeComboBoxModel_1.4.java
		File fl = new File("src/test/resources/examples/t_204225/left_UMLModelElementStereotypeComboBoxModel_1.3.java");
		File fr = new File("src/test/resources/examples/t_204225/right_UMLModelElementStereotypeComboBoxModel_1.4.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		diff.printActions(actions);
		assertEquals(actions.size(), 2);
		assertTrue(diff.containsAction(actions, "Insert", "BinaryOperator", "OR"));
		assertTrue(diff.containsAction(actions, "Move", "BinaryOperator", "AND"));
	}

	// @Test
	//bug in Spoon
	public void test_t_208618() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_208618/left_PropPanelUseCase_1.39.java src/test/resources/examples/t_208618/right_PropPanelUseCase_1.40.java
		File fl = new File("src/test/resources/examples/t_208618/left_PropPanelUseCase_1.39.java");
		File fr = new File("src/test/resources/examples/t_208618/right_PropPanelUseCase_1.40.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		diff.printActions(actions);
		assertEquals(actions.size(), 2);
		assertTrue(diff.containsAction(actions, "Insert", "Invocation", "addField"));
	}

	@Test
	public void test_t_209184() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_209184/left_ActionCollaborationDiagram_1.28.java src/test/resources/examples/t_209184/right_ActionCollaborationDiagram_1.29.java
		File fl = new File("src/test/resources/examples/t_209184/left_ActionCollaborationDiagram_1.28.java");
		File fr = new File("src/test/resources/examples/t_209184/right_ActionCollaborationDiagram_1.29.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		diff.printActions(actions);
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "UPD", "Invocation", "getTarget"));
	}

	@Test
	public void test_t_211903() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_211903/left_MemberFilePersister_1.4.java src/test/resources/examples/t_211903/right_MemberFilePersister_1.5.java
		File fl = new File("src/test/resources/examples/t_211903/left_MemberFilePersister_1.4.java");
		File fr = new File("src/test/resources/examples/t_211903/right_MemberFilePersister_1.5.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		diff.printActions(actions);
		assertEquals(actions.size(), 4);
		assertTrue(diff.containsAction(actions, "Update", "ConstructorCall", "java.io.FileReader"));
		assertTrue(diff.containsAction(actions, "Insert", "Literal", "\"UTF-8\""));
		assertTrue(diff.containsAction(actions, "Insert", "ConstructorCall", "java.io.FileInputStream"));
		assertTrue(diff.containsAction(actions, "Move", "VariableRead", "file"));
		
		// the change is in the local variable declaration
		CtElement elem = (CtElement) actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtLocalVariable.class));
	}
	
	@Test
	public void test_t_212496() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_212496/left_CoreHelperImpl_1.29.java src/test/resources/examples/t_212496/right_CoreHelperImpl_1.30.java
		File fl = new File("src/test/resources/examples/t_212496/left_CoreHelperImpl_1.29.java");
		File fr = new File("src/test/resources/examples/t_212496/right_CoreHelperImpl_1.30.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		diff.printActions(actions);
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Insert", "Method", "setEnumerationLiterals"));
	}

	@Test
	public void test_t_214116() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_214116/left_Modeller_1.134.java src/test/resources/examples/t_214116/right_Modeller_1.135.java
		File fl = new File("src/test/resources/examples/t_214116/left_Modeller_1.134.java");
		File fr = new File("src/test/resources/examples/t_214116/right_Modeller_1.135.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		diff.printActions(actions);
		assertEquals(actions.size(), 2);
		assertTrue(diff.containsAction(actions, "Update", "Literal", "\" \""));
		
		// the change is in a throw
		CtElement elem = (CtElement) actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtThrow.class));

	}



}
