package fr.inria.sacha.spoon.diffSpoon;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import fr.labri.gumtree.actions.model.Action;
import spoon.Launcher;
import spoon.compiler.SpoonCompiler;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtSimpleType;
import spoon.reflect.factory.Factory;
import spoon.support.compiler.jdt.JDTSnippetCompiler;

/**
 * Test Spoon Diff 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class DiffSpoonTest {

	
	@Test
	public void testgetCtType() throws Exception {
		String c1 = "package spoon1.test; import org.junit.Test; " 
	+ "" + "class X {" + "public void foo0() {" + " int x = 0;"
				+ "}" + "};";
		DiffSpoon diff = new DiffSpoon(true);
		CtSimpleType<?> t1 = diff.getCtType(c1);
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
		File fl = new File(getClass().
				getResource("/examples/test1/TypeHandler1.java").getFile());
		File fr = new File(getClass().
				getResource("/examples/test1/TypeHandler2.java").getFile());
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 2);
		assertTrue(containsAction(actions, "INS", "Invocation"));
		assertFalse(containsAction(actions, "DEL", "Invocation"));
		assertFalse(containsAction(actions, "UPD", "Invocation"));
		
		assertTrue(containsAction(actions, "UPD", "FieldAccess"));
	}
	
	
	@Test
	public void exampleSingleUpdate() throws Exception{
		
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File(getClass().
				getResource("/examples/test2/CommandLine1.java").getFile());
		File fr = new File(getClass().
				getResource("/examples/test2/CommandLine2.java").getFile());
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 1);
		assertTrue(containsAction(actions, "UPD", "Literal"/*"PAR-Literal"*/));
	
	}
	
	@Test
	public void exampleRemoveMethod() throws Exception{
		
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File(getClass().
				getResource("/examples/test3/CommandLine1.java").getFile());
		File fr = new File(getClass().
				getResource("/examples/test3/CommandLine2.java").getFile());
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 1);
		assertTrue(containsAction(actions, "DEL", "Method"));
	}
	
	
	@Test
	public void exampleInsert() throws Exception{
		
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File(getClass().
				getResource("/examples/test4/CommandLine1.java").getFile());
		File fr = new File(getClass().
				getResource("/examples/test4/CommandLine2.java").getFile());
	
		CtDiff result = diff.compare(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 1);
		assertTrue(containsAction(actions, "INS", "Method"));
	}
	
	@Test
	public void testMain() throws Exception{
		
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File(getClass().
				getResource("/examples/test4/CommandLine1.java").getFile());
		File fr = new File(getClass().
				getResource("/examples/test4/CommandLine2.java").getFile());
	
		DiffSpoon.main(new String []{fl.getAbsolutePath(), fr.getAbsolutePath()});
	}
	@Test
	public void testContent() throws Exception{
		File fl = new File(getClass().
				getResource("/examples/test4/CommandLine1.java").getFile());
		File fr = new File(getClass().
				getResource("/examples/test4/CommandLine2.java").getFile());
		DiffSpoon diff = new DiffSpoon(true);
		CtSimpleType ctl = diff.getSpoonType(diff.readFile(fl));
		assertNotNull(ctl);
	}
	
	private boolean containsAction(List<Action> actions, String kindAction, String kindNode){
		//TODO: the kind of the action is not visible, To see in the next version of GumTree
		for (Action action : actions) {
			String toSt = action.toString();
			if(toSt.startsWith(kindAction)){
				return action.getNode().getTypeLabel().endsWith(kindNode);
			}
		}
		return false;
	}
	
	@Test
	public void testJDTBasedSpoonCompiler(){
		
	/*	String c1 = "package spoon1.test; import org.junit.Test; " + "class X {" + "public void foo0() {" + " int x = 0;"
				+ "}" + "};";*/
		
		
		String content1 = "package spoon1.test; import org.junit.Test; " 
				+ "" + "class X {" + "public void foo0() {" + " int x = 0;"
							+ "}" + "};";
		
		Factory factory = new Launcher().createFactory();
		
		SpoonCompiler compiler = new JDTSnippetCompiler(factory, content1);//new JDTBasedSpoonCompiler(factory);
	//	compiler.addInputSource(new VirtualFile(c1,""));
		compiler.build();
		CtClass<?> clazz1 = (CtClass<?>) factory.Type().getAll().get(0);
	//	factory.Package().getAllRoots().clear();
		
		Assert.assertNotNull(clazz1);
	}
	
}
