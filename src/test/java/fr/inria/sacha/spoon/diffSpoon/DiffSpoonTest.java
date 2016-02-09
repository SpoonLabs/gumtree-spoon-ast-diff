package fr.inria.sacha.spoon.diffSpoon;


import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Move;
import org.junit.Assert;
import org.junit.Before;
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

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
		DiffSpoonImpl diff = new DiffSpoonImpl();
		CtType<?> t1 = diff.getCtType(c1);
		assertTrue(t1 != null);
	
	}
	
	@Test
	public void testAnalyzeStringString() {
		String c1 = "" + "class X {" + "public void foo0() {" + " int x = 0;"
				+ "}" + "};";
		
		String c2 = "" + "class X {" + "public void foo1() {" + " int x = 0;"
				+ "}" + "};";
		
		
		DiffSpoonImpl diff = new DiffSpoonImpl();
		CtDiffImpl editScript = diff.compare(c1, c2);
		assertTrue(editScript.getRootActions().size() == 1);
	}


	@Test
	public void exampleInsertAndUpdate() throws Exception{
		
		DiffSpoon diff = new DiffSpoonImpl();
		// meld src/test/resources/examples/test1/TypeHandler1.java src/test/resources/examples/test1/TypeHandler2.java
		File fl = new File("src/test/resources/examples/test1/TypeHandler1.java");
		File fr = new File("src/test/resources/examples/test1/TypeHandler2.java");
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actions.size());

		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtClass);
		assertTrue(result.containsAction("INS", "Invocation"));
		assertTrue(result.containsAction("UPD", "FieldRead"));
		
		assertFalse(result.containsAction("DEL", "Invocation"));
		assertFalse(result.containsAction("UPD", "Invocation"));
		
	}
	
	
	@Test
	public void exampleSingleUpdate() throws Exception{
		
		DiffSpoon diff = new DiffSpoonImpl();
		File fl = new File("src/test/resources/examples/test2/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test2/CommandLine2.java");
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("UPD", "Literal"/*"PAR-Literal"*/));
	
	}
	
	@Test
	public void exampleRemoveMethod() throws Exception{
		
		DiffSpoon diff = new DiffSpoonImpl();
		File fl = new File("src/test/resources/examples/test3/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test3/CommandLine2.java");
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		// commenting the assertion on the number of actions
		// we now have three actions, with two updates of invocations because of binding to the ol/new method
		// while it is not visible in the AST, this is indeed a change in the behavior
		// it means that the AST diff in this case also captures something deeper
		// assertEquals(1, actions.size());
		assertTrue(result.containsAction("DEL", "Method"));
	}
	
	
	@Test
	public void exampleInsert() throws Exception{
		
		DiffSpoon diff = new DiffSpoonImpl();
		File fl = new File("src/test/resources/examples/test4/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test4/CommandLine2.java");
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("INS", "Method","resolveOptionNew"));
	}
	
	@Test
	public void testMain() throws Exception{
		
		DiffSpoon diff = new DiffSpoonImpl();
		File fl = new File("src/test/resources/examples/test4/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test4/CommandLine2.java");
	
		DiffSpoonImpl.main(new String []{fl.getAbsolutePath(), fr.getAbsolutePath()});
	}
	@Test
	public void testContent() throws Exception{
		File fl = new File("src/test/resources/examples/test4/CommandLine1.java");
		File fr = new File("src/test/resources/examples/test4/CommandLine2.java");
		DiffSpoonImpl diff = new DiffSpoonImpl();
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
		DiffSpoon diff = new DiffSpoonImpl();
		File fl = new File("src/test/resources/examples/test5/left_LmiInitialContext_1.5.java");
		File fr = new File("src/test/resources/examples/test5/right_LmiInitialContext_1.6.java");
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("UPD", "BinaryOperator","AND"));
	}
	
	@Test
	public void test6() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		File fl = new File("src/test/resources/examples/test6/A.java");
		File fr = new File("src/test/resources/examples/test6/B.java");
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("DEL", "Parameter","i"));
	}

	@Test
	public void test7() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld src/test/resources/examples/test7/left_QuickNotepad_1.13.java src/test/resources/examples/test7/right_QuickNotepad_1.14.java
		File fl = new File("src/test/resources/examples/test7/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/test7/right_QuickNotepad_1.14.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsAction("DEL", "Invocation", "QuickNotepadTextArea#addKeyListener(QuickNotepad$KeyHandler)"));
		assertTrue(result.containsAction("DEL", "Class","KeyHandler"));
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtClass);
		assertEquals("QuickNotepad", ((CtClass)ancestor).getSimpleName());

	}

	@Test
	public void test_t_286700() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld src/test/resources/examples/t_286700/left_CmiContext_1.2.java src/test/resources/examples/t_286700/right_CmiContext_1.3.java
		File fl = new File("src/test/resources/examples/t_286700/left_CmiContext_1.2.java");
		File fr = new File("src/test/resources/examples/t_286700/right_CmiContext_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertTrue(result.containsAction("INS", "Method", "getObjectPort"));
		// commented for the same reason as exampleRemoveMethod
		// assertEquals(1, actions.size());
		
	}

	@Test
	public void test_t_202564() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_202564/left_PropPanelModelElement_1.9.java src/test/resources/examples/t_202564/right_PropPanelModelElement_1.10.java
		File fl = new File("src/test/resources/examples/t_202564/left_PropPanelModelElement_1.9.java");
		File fr = new File("src/test/resources/examples/t_202564/right_PropPanelModelElement_1.10.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Insert", "Field", "_assocEndRoleIcon"));
	}

	@Test
	public void test_t_204225() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_204225/left_UMLModelElementStereotypeComboBoxModel_1.3.java src/test/resources/examples/t_204225/right_UMLModelElementStereotypeComboBoxModel_1.4.java
		File fl = new File("src/test/resources/examples/t_204225/left_UMLModelElementStereotypeComboBoxModel_1.3.java");
		File fr = new File("src/test/resources/examples/t_204225/right_UMLModelElementStereotypeComboBoxModel_1.4.java");
		CtDiff result = diff.compare(fl,fr);
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtReturn);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsAction("Insert", "BinaryOperator", "OR"));
		assertTrue(result.containsAction("Move", "BinaryOperator", "AND"));
		
		
	}

	 @Test
	public void test_t_208618() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_208618/left_PropPanelUseCase_1.39.java src/test/resources/examples/t_208618/right_PropPanelUseCase_1.40.java
		File fl = new File("src/test/resources/examples/t_208618/left_PropPanelUseCase_1.39.java");
		File fr = new File("src/test/resources/examples/t_208618/right_PropPanelUseCase_1.40.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Insert", "Invocation", "#addField(<unknown>, <unknown>)"));
	}

	@Test
	public void test_t_209184() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_209184/left_ActionCollaborationDiagram_1.28.java src/test/resources/examples/t_209184/right_ActionCollaborationDiagram_1.29.java
		File fl = new File("src/test/resources/examples/t_209184/left_ActionCollaborationDiagram_1.28.java");
		File fr = new File("src/test/resources/examples/t_209184/right_ActionCollaborationDiagram_1.29.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("UPD", "Invocation", "#getTarget()"));
	}

	@Test
	public void test_t_211903() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_211903/left_MemberFilePersister_1.4.java src/test/resources/examples/t_211903/right_MemberFilePersister_1.5.java
		File fl = new File("src/test/resources/examples/t_211903/left_MemberFilePersister_1.4.java");
		File fr = new File("src/test/resources/examples/t_211903/right_MemberFilePersister_1.5.java");
		CtDiffImpl result = diff.compare(fl,fr);
		
		result.debugInformation();

		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtConstructorCall);
		assertEquals(88,ancestor.getPosition().getLine());

		
		List<Action> actions = result.getRootActions();
		assertTrue(result.containsAction("Update", "ConstructorCall", "java.io.FileReader#FileReader(java.io.File)"));
		assertTrue(result.containsAction("Insert", "ConstructorCall", "java.io.InputStreamReader#InputStreamReader(java.io.InputStream, java.lang.String)"));
		
		// additional checks on low-level actions
		assertTrue(result.containsAction(result.getAllActions(), "Insert", "Literal", "\"UTF-8\""));

		
		// the change is in the local variable declaration
		CtElement elem = (CtElement) actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtLocalVariable.class));
	}
	
	@Test
	public void test_t_212496() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_212496/left_CoreHelperImpl_1.29.java src/test/resources/examples/t_212496/right_CoreHelperImpl_1.30.java
		File fl = new File("src/test/resources/examples/t_212496/left_CoreHelperImpl_1.29.java");
		File fr = new File("src/test/resources/examples/t_212496/right_CoreHelperImpl_1.30.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Insert", "Method", "setEnumerationLiterals"));
	}

	@Test
	public void test_t_214116() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_214116/left_Modeller_1.134.java src/test/resources/examples/t_214116/right_Modeller_1.135.java
		File fl = new File("src/test/resources/examples/t_214116/left_Modeller_1.134.java");
		File fr = new File("src/test/resources/examples/t_214116/right_Modeller_1.135.java");
		CtDiff result = diff.compare(fl,fr);
		
		CtElement ancestor = result.commonAncestor();	
		assertTrue(ancestor instanceof CtBinaryOperator);

		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsAction("Update", "Literal", "\" \""));
		
		// the change is in a throw
		CtElement elem = (CtElement) actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtThrow.class));

	}

	@Test
	public void test_t_214614() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_214614/left_JXButtonGroupPanel_1.2.java src/test/resources/examples/t_214614/right_JXButtonGroupPanel_1.3.java
		File fl = new File("src/test/resources/examples/t_214614/left_JXButtonGroupPanel_1.2.java");
		File fr = new File("src/test/resources/examples/t_214614/right_JXButtonGroupPanel_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("DEL", "Invocation", "java.awt.Container#setFocusTraversalPolicyProvider(boolean)"));
	}

	@Test
	public void test_t_220985() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_220985/left_Server_1.20.java src/test/resources/examples/t_220985/right_Server_1.21.java
		File fl = new File("src/test/resources/examples/t_220985/left_Server_1.20.java");
		File fr = new File("src/test/resources/examples/t_220985/right_Server_1.21.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertTrue(result.containsAction("Insert", "Conditional"));
		
		// TODO the delete literal "." found could also be a move to the new conditional, so we don't specify this		
		// this is the case if gumtree.match.gt.minh" = "0" (but bad for other tests)
	}

	@Test
	public void test_t_221070() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_221070/left_Server_1.68.java src/test/resources/examples/t_221070/right_Server_1.69.java
		File fl = new File("src/test/resources/examples/t_221070/left_Server_1.68.java");
		File fr = new File("src/test/resources/examples/t_221070/right_Server_1.69.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("DEL", "Break"));
	}

	@Test
	public void test_t_221295() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_221295/left_Board_1.5.java src/test/resources/examples/t_221295/right_Board_1.6.java
		File fl = new File("src/test/resources/examples/t_221295/left_Board_1.5.java");
		File fr = new File("src/test/resources/examples/t_221295/right_Board_1.6.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Update", "BinaryOperator", "GT"));
		
		CtElement elem = (CtElement) actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtReturn.class));

	}

	@Test
	public void test_t_221966() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_221966/left_TurnOrdered_1.3.java src/test/resources/examples/t_221966/right_TurnOrdered_1.4.java
		File fl = new File("src/test/resources/examples/t_221966/left_TurnOrdered_1.3.java");
		File fr = new File("src/test/resources/examples/t_221966/right_TurnOrdered_1.4.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Delete", "Invocation", "java.io.PrintStream#println(char[])"));
	}

	@Test
	public void test_t_221343() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_221343/left_Server_1.186.java src/test/resources/examples/t_221343/right_Server_1.187.java
		File fl = new File("src/test/resources/examples/t_221343/left_Server_1.186.java");
		File fr = new File("src/test/resources/examples/t_221343/right_Server_1.187.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Update", "Invocation", "java.util.Vector#remove(int)"));
	}
	
	@Test
	public void test_t_221345() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_221345/left_Server_1.187.java src/test/resources/examples/t_221345/right_Server_1.188.java
		File fl = new File("src/test/resources/examples/t_221345/left_Server_1.187.java");
		File fr = new File("src/test/resources/examples/t_221345/right_Server_1.188.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Update", "Invocation", "java.util.Vector#removeElement(java.lang.Object)"));
	}

	@Test
	public void test_t_221422() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_221422/left_Server_1.227.java src/test/resources/examples/t_221422/right_Server_1.228.java
		File fl = new File("src/test/resources/examples/t_221422/left_Server_1.227.java");
		File fr = new File("src/test/resources/examples/t_221422/right_Server_1.228.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Update", "Invocation", "java.util.Vector#add(E)"));
	}

	@Test
	public void test_t_221958() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_221958/left_TilesetManager_1.22.java src/test/resources/examples/t_221958/right_TilesetManager_1.23.java
		File fl = new File("src/test/resources/examples/t_221958/left_TilesetManager_1.22.java");
		File fr = new File("src/test/resources/examples/t_221958/right_TilesetManager_1.23.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Insert", "Literal", "null"));
		
		CtElement elem = (CtElement) actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtReturn.class));

	}

	@Test
	public void test_t_222361() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_222361/left_CommonSettingsDialog_1.22.java src/test/resources/examples/t_222361/right_CommonSettingsDialog_1.23.java
		File fl = new File("src/test/resources/examples/t_222361/left_CommonSettingsDialog_1.22.java");
		File fr = new File("src/test/resources/examples/t_222361/right_CommonSettingsDialog_1.23.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Update", "Literal", "\"By holding down CTL and dragging.\""));
	}

	@Test
	public void test_t_222399() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_222399/left_TdbFile_1.7.java src/test/resources/examples/t_222399/right_TdbFile_1.8.java
		File fl = new File("src/test/resources/examples/t_222399/left_TdbFile_1.7.java");
		File fr = new File("src/test/resources/examples/t_222399/right_TdbFile_1.8.java");
		CtDiff result = diff.compare(fl,fr);
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtIf);
		assertEquals(229,ancestor.getPosition().getLine());

		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(3, actions.size());
		assertEquals(229,ancestor.getPosition().getLine());
		
		assertTrue(result.containsAction("Update", "Invocation", "#equals(java.lang.String)"));
		assertTrue(result.containsAction("Insert", "BinaryOperator", "NE"));
		assertTrue(result.containsAction("Move", "Invocation", "#equals(java.lang.String)"));

		// updated the if condition
		CtElement elem = (CtElement) actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		assertNotNull(elem);
		assertNotNull(elem.getParent(CtIf.class));

	}

	@Test
	public void test_t_222884() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_222884/left_MechView_1.21.java src/test/resources/examples/t_222884/right_MechView_1.22.java
		File fl = new File("src/test/resources/examples/t_222884/left_MechView_1.21.java");
		File fr = new File("src/test/resources/examples/t_222884/right_MechView_1.22.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Insert", "Invocation", "#append(java.lang.String)"));
	}
	
	@Test
	public void test_t_222894() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_222894/left_Client_1.150.java src/test/resources/examples/t_222894/right_Client_1.151.java
		File fl = new File("src/test/resources/examples/t_222894/left_Client_1.150.java");
		File fr = new File("src/test/resources/examples/t_222894/right_Client_1.151.java");
		CtDiff result = diff.compare(fl,fr);
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtIf);

		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertTrue(result.containsAction("Insert", "BinaryOperator", "AND"));
		
		// TODO there is a move that is not detected but should be
		// assertTrue(result.containsAction("Move", VariableRead", "Settings.keepServerlog"));
		// this is the case if gumtree.match.gt.minh" = "0" (but bad for other tests)
	}

	@Test
	public void test_t_223054() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_223054/left_GameEvent_1.2.java src/test/resources/examples/t_223054/right_GameEvent_1.3.java
		File fl = new File("src/test/resources/examples/t_223054/left_GameEvent_1.2.java");
		File fr = new File("src/test/resources/examples/t_223054/right_GameEvent_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Update", "Field", "GAME_NEW_ATTACK"));
	}

	@Test
	public void test_t_223056() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_223056/left_Server_1.646.java src/test/resources/examples/t_223056/right_Server_1.647.java
		File fl = new File("src/test/resources/examples/t_223056/left_Server_1.646.java");
		File fr = new File("src/test/resources/examples/t_223056/right_Server_1.647.java");
		CtDiff result = diff.compare(fl,fr);
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtClass);

		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsAction("Update", "Literal", "\" \""));
		assertTrue(result.containsAction("Update", "Literal","\"        \\n\""));
	}

	@Test
	public void test_t_223118() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_223118/left_TestBot_1.48.java src/test/resources/examples/t_223118/right_TestBot_1.49.java
		File fl = new File("src/test/resources/examples/t_223118/left_TestBot_1.48.java");
		File fr = new File("src/test/resources/examples/t_223118/right_TestBot_1.49.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Insert", "Invocation", "megamek.client.bot.CEntity#refresh()"));
	}

	@Test
	public void test_t_223454() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_223454/left_EntityListFile_1.17.java src/test/resources/examples/t_223454/right_EntityListFile_1.18.java
		File fl = new File("src/test/resources/examples/t_223454/left_EntityListFile_1.17.java");
		File fr = new File("src/test/resources/examples/t_223454/right_EntityListFile_1.18.java");
		CtDiffImpl result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Update", "ConstructorCall", "java.io.FileInputStream#FileInputStream(java.io.File, java.lang.String)"	));
		assertTrue(result.containsAction(result.getAllActions(), "DEL", "Literal", "\"UTF-8\""));
			
		assertEquals(441, result.changedNode().getPosition().getLine());
	}

	@Test
	public void test_t_223542() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_223542/left_BoardView1_1.214.java src/test/resources/examples/t_223542/right_BoardView1_1.215.java
		File fl = new File("src/test/resources/examples/t_223542/left_BoardView1_1.214.java");
		File fr = new File("src/test/resources/examples/t_223542/right_BoardView1_1.215.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Update", "FieldRead", "MOVE_VTOL_RUN"));
	}

	@Test
	public void test_t_224512() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_224512/left_Server_1.925.java src/test/resources/examples/t_224512/right_Server_1.926.java
		File fl = new File("src/test/resources/examples/t_224512/left_Server_1.925.java");
		File fr = new File("src/test/resources/examples/t_224512/right_Server_1.926.java");
		CtDiff result = diff.compare(fl,fr);
		
		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtBinaryOperator);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsAction("Insert", "BinaryOperator", "AND"));
		assertTrue(result.containsAction("Move", "BinaryOperator", "AND"));
	}
	
	@Test
	public void test_t_224542() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_224542/left_TestBot_1.75.java src/test/resources/examples/t_224542/right_TestBot_1.76.java
		File fl = new File("src/test/resources/examples/t_224542/left_TestBot_1.75.java");
		File fr = new File("src/test/resources/examples/t_224542/right_TestBot_1.76.java");
		CtDiff result = diff.compare(fl,fr);
		
		result.debugInformation();

		CtElement ancestor = result.commonAncestor();		
		assertTrue(ancestor instanceof CtInvocation);
		assertEquals("println", ((CtInvocation)ancestor).getExecutable().getSimpleName());
		assertEquals(344,ancestor.getPosition().getLine());
		
		List<Action> actions = result.getRootActions();
		assertEquals(3, actions.size());
		assertTrue(result.containsAction("Delete", "Invocation", "java.lang.String#format(java.lang.String, java.lang.Object[])"));
		assertTrue(result.containsAction("Insert", "BinaryOperator", "PLUS"));
		
		// the move can be either getEntity or getShortName
		assertTrue(result.containsAction("Move", "Invocation"));
		assertEquals(344, result.changedNode(Move.class).getPosition().getLine());
				
	}


	@Test
	public void test_t_224766() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_224766/left_SegmentTermEnum_1.1.java src/test/resources/examples/t_224766/right_SegmentTermEnum_1.2.java
		File fl = new File("src/test/resources/examples/t_224766/left_SegmentTermEnum_1.1.java");
		File fr = new File("src/test/resources/examples/t_224766/right_SegmentTermEnum_1.2.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsAction("Insert", "If"));
		assertTrue(result.containsAction("Move", "Invocation", "org.apache.lucene.index.SegmentTermEnum#growBuffer(int)"));
	}

	@Test 
	public void test_t_224771() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_224771/left_IndexWriter_1.2.java src/test/resources/examples/t_224771/right_IndexWriter_1.3.java
		File fl = new File("src/test/resources/examples/t_224771/left_IndexWriter_1.2.java");
		File fr = new File("src/test/resources/examples/t_224771/right_IndexWriter_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 2);
		assertTrue(result.containsAction("Insert", "BinaryOperator", "OR"));
		assertTrue(result.containsAction("Move", "Invocation", "SegmentReader#hasDeletions()"));
	}

	@Test
	public void test_t_224798() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_224798/left_SegmentsReader_1.4.java src/test/resources/examples/t_224798/right_SegmentsReader_1.5.java
		File fl = new File("src/test/resources/examples/t_224798/left_SegmentsReader_1.4.java");
		File fr = new File("src/test/resources/examples/t_224798/right_SegmentsReader_1.5.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Update", "Invocation", "#delete(int)" ));
	}
	
	@Test
	public void test_t_224834() throws Exception{
		// wonderful example where the text diff is impossible to  comprehend
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_224834/left_TestPriorityQueue_1.2.java src/test/resources/examples/t_224834/right_TestPriorityQueue_1.3.java
		File fl = new File("src/test/resources/examples/t_224834/left_TestPriorityQueue_1.2.java");
		File fr = new File("src/test/resources/examples/t_224834/right_TestPriorityQueue_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Insert", "Method", "testClear"));
	}

	@Test
	public void test_t_224863() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_224863/left_PhraseQuery_1.4.java src/test/resources/examples/t_224863/right_PhraseQuery_1.5.java
		File fl = new File("src/test/resources/examples/t_224863/left_PhraseQuery_1.4.java");
		File fr = new File("src/test/resources/examples/t_224863/right_PhraseQuery_1.5.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Insert", "Assignment"));
		
		// the change is in the block that starts at line110
		assertEquals(110, result.changedNode().getPosition().getLine());
		
		// and the new element is at line 111
		assertEquals(111, ((CtElement)actions.get(0).getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT)).getPosition().getLine());
	}

	@Test
	public void test_t_224882() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_224882/left_Token_1.3.java src/test/resources/examples/t_224882/right_Token_1.4.java
		File fl = new File("src/test/resources/examples/t_224882/left_Token_1.3.java");
		File fr = new File("src/test/resources/examples/t_224882/right_Token_1.4.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("UPD", "Literal", "\"Increment must be positive: \""));
	}

	@Test
	public void test_t_224890() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_224890/left_DateField_1.4.java src/test/resources/examples/t_224890/right_DateField_1.5.java
		File fl = new File("src/test/resources/examples/t_224890/left_DateField_1.4.java");
		File fr = new File("src/test/resources/examples/t_224890/right_DateField_1.5.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actions.size());
		assertTrue(result.containsAction("UPD", "Literal", "' '"));
		assertTrue(result.containsAction("UPD", "Invocation", "java.lang.StringBuffer#insert(int, char)"));
	}

	@Test
	public void test_t_225008() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225008/left_Similarity_1.9.java src/test/resources/examples/t_225008/right_Similarity_1.10.java
		File fl = new File("src/test/resources/examples/t_225008/left_Similarity_1.9.java");
		File fr = new File("src/test/resources/examples/t_225008/right_Similarity_1.10.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Update", "Modifier", "protected"));
		
		// TODO regression in Spoon on line numbers
		// assertEquals(324, result.changedNode().getPosition().getLine());
	}

	@Test
	public void test_t_225073() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225073/left_IndexWriter_1.21.java src/test/resources/examples/t_225073/right_IndexWriter_1.22.java
		File fl = new File("src/test/resources/examples/t_225073/left_IndexWriter_1.21.java");
		File fr = new File("src/test/resources/examples/t_225073/right_IndexWriter_1.22.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Insert", "VariableRead", "COMMIT_LOCK_TIMEOUT"));
		// the change is in a constructor call
		assertTrue(result.changedNode() instanceof CtConstructorCall);
	}
	
	@Test
	public void test_t_286696() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_286696/left_IrmiPRODelegate_1.2.java src/test/resources/examples/t_286696/right_IrmiPRODelegate_1.3.java
		File fl = new File("src/test/resources/examples/t_286696/left_IrmiPRODelegate_1.2.java");
		File fr = new File("src/test/resources/examples/t_286696/right_IrmiPRODelegate_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(actions.size(), 1);
		assertTrue(result.containsAction("Update", "FieldRead", "SERVER_JRMP_PORT"));
	}

	@Test
	public void test_t_225106() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225106/left_SegmentTermDocs_1.6.java src/test/resources/examples/t_225106/right_SegmentTermDocs_1.7.java
		File fl = new File("src/test/resources/examples/t_225106/left_SegmentTermDocs_1.6.java");
		File fr = new File("src/test/resources/examples/t_225106/right_SegmentTermDocs_1.7.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Update", "BinaryOperator", "GT"));
	}

	@Test 
	public void test_t_213712() throws Exception{
		// works with gumtree.match.gt.minh = 1 (and not the default 2)
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_213712/left_ActionAddSignalsToSignalEvent_1.2.java src/test/resources/examples/t_213712/right_ActionAddSignalsToSignalEvent_1.3.java
		File fl = new File("src/test/resources/examples/t_213712/left_ActionAddSignalsToSignalEvent_1.2.java");
		File fr = new File("src/test/resources/examples/t_213712/right_ActionAddSignalsToSignalEvent_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(3, actions.size());
		assertTrue(result.containsAction("Insert", "Field", "serialVersionUID"));
		assertTrue(result.containsAction("Insert", "Block"));
		assertTrue(result.containsAction("Move", "Invocation", "java.util.Vector#add(E)" ));

	}

	@Test
	public void test_t_225225() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225225/left_TestSpans_1.3.java src/test/resources/examples/t_225225/right_TestSpans_1.4.java
		File fl = new File("src/test/resources/examples/t_225225/left_TestSpans_1.3.java");
		File fr = new File("src/test/resources/examples/t_225225/right_TestSpans_1.4.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("DEL", "LocalVariable", "buffer"));
	}

	@Test
	public void test_t_225247() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225247/left_BooleanScorer_1.10.java src/test/resources/examples/t_225247/right_BooleanScorer_1.11.java
		File fl = new File("src/test/resources/examples/t_225247/left_BooleanScorer_1.10.java");
		File fr = new File("src/test/resources/examples/t_225247/right_BooleanScorer_1.11.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Update", "BinaryOperator", "BITOR"));
	}

	
	@Test 
	public void test_t_225262() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225262/left_FieldInfos_1.9.java src/test/resources/examples/t_225262/right_FieldInfos_1.10.java
		File fl = new File("src/test/resources/examples/t_225262/left_FieldInfos_1.9.java");
		File fr = new File("src/test/resources/examples/t_225262/right_FieldInfos_1.10.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actions.size());
		assertTrue(result.containsAction("Insert", "Block"));
		assertTrue(result.containsAction("Move", "Assignment" ));
	}

	@Test
	public void test_t_225391() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225391/left_IndexHTML_1.4.java src/test/resources/examples/t_225391/right_IndexHTML_1.5.java
		File fl = new File("src/test/resources/examples/t_225391/left_IndexHTML_1.4.java");
		File fr = new File("src/test/resources/examples/t_225391/right_IndexHTML_1.5.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(3, actions.size());
		assertTrue(result.containsAction("Delete", "Assignment"));
		assertTrue(result.containsAction("Insert", "Invocation", "org.apache.lucene.index.IndexWriter#setMaxFieldLength(int)" ));
		assertTrue(result.containsAction("Move", "FieldRead", "writer" ));
	}
	
	@Test
	public void test_t_225414() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225414/left_IndexWriter_1.41.java src/test/resources/examples/t_225414/right_IndexWriter_1.42.java
		File fl = new File("src/test/resources/examples/t_225414/left_IndexWriter_1.41.java");
		File fr = new File("src/test/resources/examples/t_225414/right_IndexWriter_1.42.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Update", "Invocation", "java.lang.Throwable#getMessage()"));
	}

	@Test
	public void test_t_225434() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225434/left_BufferedIndexInput_1.2.java src/test/resources/examples/t_225434/right_BufferedIndexInput_1.3.java
		File fl = new File("src/test/resources/examples/t_225434/left_BufferedIndexInput_1.2.java");
		File fr = new File("src/test/resources/examples/t_225434/right_BufferedIndexInput_1.3.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Update", "BinaryOperator", "EQ"));
	}

	@Test
	public void test_t_225525() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225525/left_Module_1.6.java src/test/resources/examples/t_225525/right_Module_1.7.java
		File fl = new File("src/test/resources/examples/t_225525/left_Module_1.6.java");
		File fr = new File("src/test/resources/examples/t_225525/right_Module_1.7.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Insert", "Method", "getAttributes" ));
	}
	
	@Test
	public void test_t_225724() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225724/left_ScarabRequestTool_1.36.java src/test/resources/examples/t_225724/right_ScarabRequestTool_1.37.java
		File fl = new File("src/test/resources/examples/t_225724/left_ScarabRequestTool_1.36.java");
		File fr = new File("src/test/resources/examples/t_225724/right_ScarabRequestTool_1.37.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Update", "Invocation", "org.apache.turbine.util.Log#error(java.lang.String, java.lang.Exception)"));
	}
	
	@Test
	public void test_t_225893() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_225893/left_RQueryUser_1.1.java src/test/resources/examples/t_225893/right_RQueryUser_1.2.java
		File fl = new File("src/test/resources/examples/t_225893/left_RQueryUser_1.1.java");
		File fr = new File("src/test/resources/examples/t_225893/right_RQueryUser_1.2.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Insert", "Method", "delete"));
	}

	@Test
	public void test_t_226145() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_226145/left_ScarabRequestTool_1.90.java src/test/resources/examples/t_226145/right_ScarabRequestTool_1.91.java
		File fl = new File("src/test/resources/examples/t_226145/left_ScarabRequestTool_1.90.java");
		File fr = new File("src/test/resources/examples/t_226145/right_ScarabRequestTool_1.91.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Insert", "Method", "getIssueByUniqueId"));
	}

	@Test
	public void test_t_226330() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_226330/left_ActivityRule_1.4.java src/test/resources/examples/t_226330/right_ActivityRule_1.5.java
		File fl = new File("src/test/resources/examples/t_226330/left_ActivityRule_1.4.java");
		File fr = new File("src/test/resources/examples/t_226330/right_ActivityRule_1.5.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Update", "TypeAccess", "DBImport.STATE_DB_INSERTION"));
	}

	@Test
	public void test_t_226480() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_226480/left_ScarabRequestTool_1.113.java src/test/resources/examples/t_226480/right_ScarabRequestTool_1.114.java
		File fl = new File("src/test/resources/examples/t_226480/left_ScarabRequestTool_1.113.java");
		File fr = new File("src/test/resources/examples/t_226480/right_ScarabRequestTool_1.114.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Insert", "Invocation", "org.apache.turbine.Log#debug(java.lang.String)"));
	}
	
	@Test
	public void test_t_226555() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_226555/left_Attachment_1.24.java src/test/resources/examples/t_226555/right_Attachment_1.25.java
		File fl = new File("src/test/resources/examples/t_226555/left_Attachment_1.24.java");
		File fr = new File("src/test/resources/examples/t_226555/right_Attachment_1.25.java");
		CtDiffImpl result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		// root actions
		assertTrue(result.containsAction("Update", "Invocation", "java.lang.String#lastIndexOf(java.lang.String)"));
		// low level actions
		assertTrue(result.containsAction(result.getAllActions(), "DEL", "FieldRead", "separator"));
		assertTrue(result.containsAction(result.getAllActions(), "Insert", "Literal", "'/'" ));
	}

	@Test
	public void test_t_226622() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_226622/left_AttributeValue_1.49.java src/test/resources/examples/t_226622/right_AttributeValue_1.50.java
		File fl = new File("src/test/resources/examples/t_226622/left_AttributeValue_1.49.java");
		File fr = new File("src/test/resources/examples/t_226622/right_AttributeValue_1.50.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		
		// no assert on number of actions because a move migt be detected (TODO?)
		assertTrue(result.containsAction("Insert", "BinaryOperator", "AND"));
	}

	@Test
	public void test_t_226685() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_226685/left_ResetCacheValve_1.1.java src/test/resources/examples/t_226685/right_ResetCacheValve_1.2.java
		File fl = new File("src/test/resources/examples/t_226685/left_ResetCacheValve_1.1.java");
		File fr = new File("src/test/resources/examples/t_226685/right_ResetCacheValve_1.2.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("DEL", "Invocation", "java.io.PrintStream#println(java.lang.String)"));
	}
	
	
	@Test
	public void test_t_226926() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_226926/left_ScarabUserManager_1.4.java src/test/resources/examples/t_226926/right_ScarabUserManager_1.5.java
		File fl = new File("src/test/resources/examples/t_226926/left_ScarabUserManager_1.4.java");
		File fr = new File("src/test/resources/examples/t_226926/right_ScarabUserManager_1.5.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Update", "Modifier", "public"));
	}

	
	@Test
	public void test_t_226963() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_226963/left_Issue_1.140.java src/test/resources/examples/t_226963/right_Issue_1.141.java
		File fl = new File("src/test/resources/examples/t_226963/left_Issue_1.140.java");
		File fr = new File("src/test/resources/examples/t_226963/right_Issue_1.141.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Update", "Invocation", "#addAscendingOrderByColumn()" ));
	}


	@Test
	public void test_t_227005() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_227005/left_AttributeValue_1.56.java src/test/resources/examples/t_227005/right_AttributeValue_1.57.java
		File fl = new File("src/test/resources/examples/t_227005/left_AttributeValue_1.56.java");
		File fr = new File("src/test/resources/examples/t_227005/right_AttributeValue_1.57.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actions.size());
		assertTrue(result.containsAction("Insert", "BinaryOperator", "AND"));
		assertTrue(result.containsAction("Move", "BinaryOperator", "AND"));
	}

	@Test
	public void test_t_227130() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_227130/left_Transaction_1.37.java src/test/resources/examples/t_227130/right_Transaction_1.38.java
		File fl = new File("src/test/resources/examples/t_227130/left_Transaction_1.37.java");
		File fr = new File("src/test/resources/examples/t_227130/right_Transaction_1.38.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction( "INS", "Method", "create"));
	}
	
	@Test
	public void test_t_227368() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_227368/left_IssueTemplateInfo_1.12.java src/test/resources/examples/t_227368/right_IssueTemplateInfo_1.13.java
		File fl = new File("src/test/resources/examples/t_227368/left_IssueTemplateInfo_1.12.java");
		File fr = new File("src/test/resources/examples/t_227368/right_IssueTemplateInfo_1.13.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actions.size());
		assertTrue(result.containsAction("UPD", "Invocation", "org.tigris.scarab.util.Email#sendEmail(org.apache.fulcrum.template.TemplateContext, org.tigris.scarab.om.Module, <unknown>, <unknown>, java.lang.String, java.lang.String)"));
		
		// one parameter is moved to another argument
		assertTrue(result.containsAction("Move", "Invocation"));
	}

	@Test
	public void test_t_227811() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
			// meld  src/test/resources/examples/t_227811/left_RModuleIssueType_1.24.java src/test/resources/examples/t_227811/right_RModuleIssueType_1.25.java
			File fl = new File("src/test/resources/examples/t_227811/left_RModuleIssueType_1.24.java");
		File fr = new File("src/test/resources/examples/t_227811/right_RModuleIssueType_1.25.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Insert", "Invocation", "#setDisplayDescription(java.lang.String)" ));
	}

	@Test
	public void test_t_227985() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_227985/left_IssueSearch_1.65.java src/test/resources/examples/t_227985/right_IssueSearch_1.66.java
		File fl = new File("src/test/resources/examples/t_227985/left_IssueSearch_1.65.java");
		File fr = new File("src/test/resources/examples/t_227985/right_IssueSearch_1.66.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Insert", "Assignment"));
	}
	
	@Test
	public void test_t_228064() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_228064/left_ModuleManager_1.21.java src/test/resources/examples/t_228064/right_ModuleManager_1.22.java
		File fl = new File("src/test/resources/examples/t_228064/left_ModuleManager_1.21.java");
		File fr = new File("src/test/resources/examples/t_228064/right_ModuleManager_1.22.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("UPD", "Modifier", "public"));
	}

	@Test
	public void test_t_228325() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_228325/left_ForgotPassword_1.10.java src/test/resources/examples/t_228325/right_ForgotPassword_1.11.java
		File fl = new File("src/test/resources/examples/t_228325/left_ForgotPassword_1.10.java");
		File fr = new File("src/test/resources/examples/t_228325/right_ForgotPassword_1.11.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("UPD", "Literal", "\"ForgotPassword.vm\""));
	}

	@Test
	public void test_t_228643() throws Exception{
		DiffSpoon diff = new DiffSpoonImpl();
		// meld  src/test/resources/examples/t_228643/left_ScopePeer_1.3.java src/test/resources/examples/t_228643/right_ScopePeer_1.4.java
		File fl = new File("src/test/resources/examples/t_228643/left_ScopePeer_1.3.java");
		File fr = new File("src/test/resources/examples/t_228643/right_ScopePeer_1.4.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actions = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsAction("Update", "ConstructorCall", "Criteria#Criteria()"));
	}

}
