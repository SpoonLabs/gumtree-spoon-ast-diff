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
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
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
		// meld src/test/resources/examples/test1/TypeHandler1.java src/test/resources/examples/test1/TypeHandler2.java
		File fl = new File("src/test/resources/examples/test1/TypeHandler1.java");
		File fr = new File("src/test/resources/examples/test1/TypeHandler2.java");
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 2);

		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtClass);

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
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "DEL", "Parameter","i"));
	}

	@Test
	public void test7() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld src/test/resources/examples/test7/left_QuickNotepad_1.13.java src/test/resources/examples/test7/right_QuickNotepad_1.14.java
		File fl = new File("src/test/resources/examples/test7/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/test7/right_QuickNotepad_1.14.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 2);
		assertTrue(diff.containsAction(actions, "DEL", "Invocation", "addKeyListener"));
		assertTrue(diff.containsAction(actions, "DEL", "Class","KeyHandler"));
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtClass);
		assertEquals("QuickNotepad", ((CtClass)ancestor).getSimpleName());

	}

	@Test
	public void test_t_286700() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld src/test/resources/examples/t_286700/left_CmiContext_1.2.java src/test/resources/examples/t_286700/right_CmiContext_1.3.java
		File fl = new File("src/test/resources/examples/t_286700/left_CmiContext_1.2.java");
		File fr = new File("src/test/resources/examples/t_286700/right_CmiContext_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		result.debugInformation();//TODO there is still a bug there
		assertEquals(1, actions.size());
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
		System.out.println(result.toString());
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
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtReturn);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 2);
		assertTrue(diff.containsAction(actions, "Insert", "BinaryOperator", "OR"));
		assertTrue(diff.containsAction(actions, "Move", "BinaryOperator", "AND"));
		
		
	}

	 @Test
	public void test_t_208618() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_208618/left_PropPanelUseCase_1.39.java src/test/resources/examples/t_208618/right_PropPanelUseCase_1.40.java
		File fl = new File("src/test/resources/examples/t_208618/left_PropPanelUseCase_1.39.java");
		File fr = new File("src/test/resources/examples/t_208618/right_PropPanelUseCase_1.40.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		 System.out.println(result.toString());
		assertEquals(actions.size(), 1);
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
		System.out.println(result.toString());
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
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtConstructorCall);
		assertEquals(88,ancestor.getPosition().getLine());

		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
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
		System.out.println(result.toString());
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
		
		CtElement ancestor = result.commonAncestor();	
		assertTrue(ancestor instanceof CtBinaryOperator);

		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 2);
		assertTrue(diff.containsAction(actions, "Update", "Literal", "\" \""));
		
		// the change is in a throw
		CtElement elem = (CtElement) actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtThrow.class));

	}

	@Test
	public void test_t_214614() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_214614/left_JXButtonGroupPanel_1.2.java src/test/resources/examples/t_214614/right_JXButtonGroupPanel_1.3.java
		File fl = new File("src/test/resources/examples/t_214614/left_JXButtonGroupPanel_1.2.java");
		File fr = new File("src/test/resources/examples/t_214614/right_JXButtonGroupPanel_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "DEL", "Invocation", "setFocusTraversalPolicyProvider"));
	}

	@Test
	public void test_t_220985() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_220985/left_Server_1.20.java src/test/resources/examples/t_220985/right_Server_1.21.java
		File fl = new File("src/test/resources/examples/t_220985/left_Server_1.20.java");
		File fr = new File("src/test/resources/examples/t_220985/right_Server_1.21.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertTrue(diff.containsAction(actions, "Insert", "Conditional"));
		
		// TODO the delete literal "." found could also be a move to the new conditional, so we don't specify this		
	}

	@Test
	public void test_t_221070() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_221070/left_Server_1.68.java src/test/resources/examples/t_221070/right_Server_1.69.java
		File fl = new File("src/test/resources/examples/t_221070/left_Server_1.68.java");
		File fr = new File("src/test/resources/examples/t_221070/right_Server_1.69.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "DEL", "Break"));
	}

	@Test
	public void test_t_221295() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_221295/left_Board_1.5.java src/test/resources/examples/t_221295/right_Board_1.6.java
		File fl = new File("src/test/resources/examples/t_221295/left_Board_1.5.java");
		File fr = new File("src/test/resources/examples/t_221295/right_Board_1.6.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Update", "BinaryOperator", "GT"));
		
		CtElement elem = (CtElement) actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtReturn.class));

	}

	@Test
	public void test_t_221966() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_221966/left_TurnOrdered_1.3.java src/test/resources/examples/t_221966/right_TurnOrdered_1.4.java
		File fl = new File("src/test/resources/examples/t_221966/left_TurnOrdered_1.3.java");
		File fr = new File("src/test/resources/examples/t_221966/right_TurnOrdered_1.4.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Delete", "Invocation", "println"));
	}

	@Test
	public void test_t_221343() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_221343/left_Server_1.186.java src/test/resources/examples/t_221343/right_Server_1.187.java
		File fl = new File("src/test/resources/examples/t_221343/left_Server_1.186.java");
		File fr = new File("src/test/resources/examples/t_221343/right_Server_1.187.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Update", "Invocation", "remove"));
	}
	
	@Test
	public void test_t_221345() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_221345/left_Server_1.187.java src/test/resources/examples/t_221345/right_Server_1.188.java
		File fl = new File("src/test/resources/examples/t_221345/left_Server_1.187.java");
		File fr = new File("src/test/resources/examples/t_221345/right_Server_1.188.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Update", "Invocation", "removeElement"));
	}

	@Test
	public void test_t_221422() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_221422/left_Server_1.227.java src/test/resources/examples/t_221422/right_Server_1.228.java
		File fl = new File("src/test/resources/examples/t_221422/left_Server_1.227.java");
		File fr = new File("src/test/resources/examples/t_221422/right_Server_1.228.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Update", "Invocation", "add"));
	}

	@Test
	public void test_t_221958() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_221958/left_TilesetManager_1.22.java src/test/resources/examples/t_221958/right_TilesetManager_1.23.java
		File fl = new File("src/test/resources/examples/t_221958/left_TilesetManager_1.22.java");
		File fr = new File("src/test/resources/examples/t_221958/right_TilesetManager_1.23.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Insert", "Literal", "null"));
		
		CtElement elem = (CtElement) actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtReturn.class));

	}

	@Test
	public void test_t_222361() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_222361/left_CommonSettingsDialog_1.22.java src/test/resources/examples/t_222361/right_CommonSettingsDialog_1.23.java
		File fl = new File("src/test/resources/examples/t_222361/left_CommonSettingsDialog_1.22.java");
		File fr = new File("src/test/resources/examples/t_222361/right_CommonSettingsDialog_1.23.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Update", "Literal", "\"By holding down CTL and dragging.\""));
	}

	@Test
	public void test_t_222399() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_222399/left_TdbFile_1.7.java src/test/resources/examples/t_222399/right_TdbFile_1.8.java
		File fl = new File("src/test/resources/examples/t_222399/left_TdbFile_1.7.java");
		File fr = new File("src/test/resources/examples/t_222399/right_TdbFile_1.8.java");
		CtDiff result = diff.compare(fl,fr);
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtIf);
		assertEquals(229,ancestor.getPosition().getLine());

		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 3);
		assertEquals(229,ancestor.getPosition().getLine());
		
		assertTrue(diff.containsAction(actions, "Update", "Invocation", "equals"));
		assertTrue(diff.containsAction(actions, "Insert", "BinaryOperator", "NE"));
		assertTrue(diff.containsAction(actions, "Move", "Invocation", "equals"));

		// updated the if condition
		CtElement elem = (CtElement) actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtIf.class));

	}

	@Test
	public void test_t_222884() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_222884/left_MechView_1.21.java src/test/resources/examples/t_222884/right_MechView_1.22.java
		File fl = new File("src/test/resources/examples/t_222884/left_MechView_1.21.java");
		File fr = new File("src/test/resources/examples/t_222884/right_MechView_1.22.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Insert", "Invocation", "append"));
	}
	
	@Test
	public void test_t_222894() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_222894/left_Client_1.150.java src/test/resources/examples/t_222894/right_Client_1.151.java
		File fl = new File("src/test/resources/examples/t_222894/left_Client_1.150.java");
		File fr = new File("src/test/resources/examples/t_222894/right_Client_1.151.java");
		CtDiff result = diff.compare(fl,fr);
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtIf);

		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertTrue(diff.containsAction(actions,"Insert", "BinaryOperator", "AND"));
		
		// TODO there is a move that is not detected but should be
		//assertEquals(actions.size(), 2);
		// assertTrue(diff.containsAction(actions, "Move", VariableRead", "Settings.keepServerlog"));
	}

	@Test
	public void test_t_223054() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_223054/left_GameEvent_1.2.java src/test/resources/examples/t_223054/right_GameEvent_1.3.java
		File fl = new File("src/test/resources/examples/t_223054/left_GameEvent_1.2.java");
		File fr = new File("src/test/resources/examples/t_223054/right_GameEvent_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Update", "Field", "GAME_NEW_ATTACK"));
	}

	@Test
	public void test_t_223056() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_223056/left_Server_1.646.java src/test/resources/examples/t_223056/right_Server_1.647.java
		File fl = new File("src/test/resources/examples/t_223056/left_Server_1.646.java");
		File fr = new File("src/test/resources/examples/t_223056/right_Server_1.647.java");
		CtDiff result = diff.compare(fl,fr);
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtClass);

		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 2);
		assertTrue(diff.containsAction(actions, "Update", "Literal", "\" \""));
		assertTrue(diff.containsAction(actions, "Update", "Literal","\"        \\n\""));
	}

	@Test
	public void test_t_223118() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_223118/left_TestBot_1.48.java src/test/resources/examples/t_223118/right_TestBot_1.49.java
		File fl = new File("src/test/resources/examples/t_223118/left_TestBot_1.48.java");
		File fr = new File("src/test/resources/examples/t_223118/right_TestBot_1.49.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Insert", "Invocation", "refresh"));
	}

	@Test
	public void test_t_223454() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_223454/left_EntityListFile_1.17.java src/test/resources/examples/t_223454/right_EntityListFile_1.18.java
		File fl = new File("src/test/resources/examples/t_223454/left_EntityListFile_1.17.java");
		File fr = new File("src/test/resources/examples/t_223454/right_EntityListFile_1.18.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "DEL", "Literal", "\"UTF-8\""));
		assertEquals(442, result.changedNode().getPosition().getLine());
	}

	@Test
	public void test_t_223542() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_223542/left_BoardView1_1.214.java src/test/resources/examples/t_223542/right_BoardView1_1.215.java
		File fl = new File("src/test/resources/examples/t_223542/left_BoardView1_1.214.java");
		File fr = new File("src/test/resources/examples/t_223542/right_BoardView1_1.215.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Update", "FieldRead", "IEntityMovementType.MOVE_VTOL_RUN"));
	}

	@Test
	public void test_t_224512() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_224512/left_Server_1.925.java src/test/resources/examples/t_224512/right_Server_1.926.java
		File fl = new File("src/test/resources/examples/t_224512/left_Server_1.925.java");
		File fr = new File("src/test/resources/examples/t_224512/right_Server_1.926.java");
		CtDiff result = diff.compare(fl,fr);
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtBinaryOperator);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 2);
		assertTrue(diff.containsAction(actions, "Insert", "BinaryOperator", "AND"));
		assertTrue(diff.containsAction(actions, "Move", "BinaryOperator", "AND"));
	}
	
	@Test
	public void test_t_224542() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_224542/left_TestBot_1.75.java src/test/resources/examples/t_224542/right_TestBot_1.76.java
		File fl = new File("src/test/resources/examples/t_224542/left_TestBot_1.75.java");
		File fr = new File("src/test/resources/examples/t_224542/right_TestBot_1.76.java");
		CtDiff result = diff.compare(fl,fr);
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtInvocation);
		assertEquals("println", ((CtInvocation)ancestor).getExecutable().getSimpleName());
		assertEquals(344,ancestor.getPosition().getLine());
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 3);
		assertTrue(diff.containsAction(actions, "Delete", "Invocation", "format"));
		assertTrue(diff.containsAction(actions, "Insert", "BinaryOperator", "PLUS"));
		assertTrue(diff.containsAction(actions, "Move", "Invocation", "getShortName"));
				
	}


	@Test
	public void test_t_224766() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_224766/left_SegmentTermEnum_1.1.java src/test/resources/examples/t_224766/right_SegmentTermEnum_1.2.java
		File fl = new File("src/test/resources/examples/t_224766/left_SegmentTermEnum_1.1.java");
		File fr = new File("src/test/resources/examples/t_224766/right_SegmentTermEnum_1.2.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 2);
		assertTrue(diff.containsAction(actions, "Insert", "If"));
		assertTrue(diff.containsAction(actions, "Move", "Invocation", "growBuffer"));
	}

	@Test 
	public void test_t_224771() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_224771/left_IndexWriter_1.2.java src/test/resources/examples/t_224771/right_IndexWriter_1.3.java
		File fl = new File("src/test/resources/examples/t_224771/left_IndexWriter_1.2.java");
		File fr = new File("src/test/resources/examples/t_224771/right_IndexWriter_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 2);
		assertTrue(diff.containsAction(actions, "Insert", "BinaryOperator", "OR"));
		assertTrue(diff.containsAction(actions, "Move", "Invocation", "hasDeletions"));
	}

	@Test
	public void test_t_224798() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_224798/left_SegmentsReader_1.4.java src/test/resources/examples/t_224798/right_SegmentsReader_1.5.java
		File fl = new File("src/test/resources/examples/t_224798/left_SegmentsReader_1.4.java");
		File fr = new File("src/test/resources/examples/t_224798/right_SegmentsReader_1.5.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Update", "Invocation", "delete" ));
	}
	
	@Test
	public void test_t_224834() throws Exception{
		// wonderful example where the text diff is impossible to  comprehend
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_224834/left_TestPriorityQueue_1.2.java src/test/resources/examples/t_224834/right_TestPriorityQueue_1.3.java
		File fl = new File("src/test/resources/examples/t_224834/left_TestPriorityQueue_1.2.java");
		File fr = new File("src/test/resources/examples/t_224834/right_TestPriorityQueue_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Insert", "Method", "testClear"));
	}

	@Test
	public void test_t_224863() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_224863/left_PhraseQuery_1.4.java src/test/resources/examples/t_224863/right_PhraseQuery_1.5.java
		File fl = new File("src/test/resources/examples/t_224863/left_PhraseQuery_1.4.java");
		File fr = new File("src/test/resources/examples/t_224863/right_PhraseQuery_1.5.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Insert", "Assignment"));
	}

	@Test
	public void test_t_224882() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_224882/left_Token_1.3.java src/test/resources/examples/t_224882/right_Token_1.4.java
		File fl = new File("src/test/resources/examples/t_224882/left_Token_1.3.java");
		File fr = new File("src/test/resources/examples/t_224882/right_Token_1.4.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "UPD", "Literal", "\"Increment must be positive: \""));
	}

	@Test
	public void test_t_224890() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_224890/left_DateField_1.4.java src/test/resources/examples/t_224890/right_DateField_1.5.java
		File fl = new File("src/test/resources/examples/t_224890/left_DateField_1.4.java");
		File fr = new File("src/test/resources/examples/t_224890/right_DateField_1.5.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "UPD", "Literal"));
		assertEquals(112, result.changedNode().getPosition().getLine());
	}

	@Test
	public void test_t_225008() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_225008/left_Similarity_1.9.java src/test/resources/examples/t_225008/right_Similarity_1.10.java
		File fl = new File("src/test/resources/examples/t_225008/left_Similarity_1.9.java");
		File fr = new File("src/test/resources/examples/t_225008/right_Similarity_1.10.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		System.out.println(result.toString());
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Update", "Modifier", "protected"));
		assertEquals(324, result.changedNode().getPosition().getLine());
	}

	@Test
	public void test_t_225073() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_225073/left_IndexWriter_1.21.java src/test/resources/examples/t_225073/right_IndexWriter_1.22.java
		File fl = new File("src/test/resources/examples/t_225073/left_IndexWriter_1.21.java");
		File fr = new File("src/test/resources/examples/t_225073/right_IndexWriter_1.22.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(diff.containsAction(actions, "Insert", "VariableRead", "COMMIT_LOCK_TIMEOUT"));
		// the change is in a constructor call
		assertTrue(result.changedNode() instanceof CtConstructorCall);
	}
	
	@Test
	public void test_t_286696() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_286696/left_IrmiPRODelegate_1.2.java src/test/resources/examples/t_286696/right_IrmiPRODelegate_1.3.java
		File fl = new File("src/test/resources/examples/t_286696/left_IrmiPRODelegate_1.2.java");
		File fr = new File("src/test/resources/examples/t_286696/right_IrmiPRODelegate_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(diff.containsAction(actions, "Update", "FieldRead", "CarolDefaultValues.SERVER_JRMP_PORT"));
	}

	@Test
	public void test_t_225106() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_225106/left_SegmentTermDocs_1.6.java src/test/resources/examples/t_225106/right_SegmentTermDocs_1.7.java
		File fl = new File("src/test/resources/examples/t_225106/left_SegmentTermDocs_1.6.java");
		File fr = new File("src/test/resources/examples/t_225106/right_SegmentTermDocs_1.7.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(diff.containsAction(actions, "Update", "BinaryOperator", "GT"));
	}

	// @Test TODO bug, should detect a move invocation
	public void test_t_213712() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_213712/left_ActionAddSignalsToSignalEvent_1.2.java src/test/resources/examples/t_213712/right_ActionAddSignalsToSignalEvent_1.3.java
		File fl = new File("src/test/resources/examples/t_213712/left_ActionAddSignalsToSignalEvent_1.2.java");
		File fr = new File("src/test/resources/examples/t_213712/right_ActionAddSignalsToSignalEvent_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actions.size());
		assertTrue(diff.containsAction(actions, "DEL", "Invocation", "addKeyListener"));
		assertTrue(diff.containsAction(actions, "DEL", "Class","KeyHandler"));
	}

	@Test
	public void test_t_225225() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_225225/left_TestSpans_1.3.java src/test/resources/examples/t_225225/right_TestSpans_1.4.java
		File fl = new File("src/test/resources/examples/t_225225/left_TestSpans_1.3.java");
		File fr = new File("src/test/resources/examples/t_225225/right_TestSpans_1.4.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(diff.containsAction(actions, "DEL", "LocalVariable", "buffer"));
	}

	@Test
	public void test_t_225247() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		// meld  src/test/resources/examples/t_225247/left_BooleanScorer_1.10.java src/test/resources/examples/t_225247/right_BooleanScorer_1.11.java
		File fl = new File("src/test/resources/examples/t_225247/left_BooleanScorer_1.10.java");
		File fr = new File("src/test/resources/examples/t_225247/right_BooleanScorer_1.11.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(diff.containsAction(actions, "Update", "BinaryOperator", "BITOR"));
	}

	
}
