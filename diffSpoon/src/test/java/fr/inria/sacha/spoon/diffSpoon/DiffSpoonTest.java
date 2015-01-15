package fr.inria.sacha.spoon.diffSpoon;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtSimpleType;
import fr.labri.gumtree.actions.Action;
/**
 * Test Spoon Diff 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class DiffSpoonTest {

	@Test
	public void testAnalyzeStringString() {
		String c1 = "" + "class X {" + "public void foo0() {" + " int x = 0;"
				+ "}" + "};";
		
		String c2 = "" + "class X {" + "public void foo1() {" + " int x = 0;"
				+ "}" + "};";
		
		
		DiffSpoon diff = new DiffSpoon(true);
		CtDiff editScript = diff.analyze(c1, c2);
		assertTrue(editScript.rootActions.size() == 1);
	}


	@Test
	public void exampleInsertAndUpdate() throws Exception{
		
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File(getClass().
				getResource("/examples/test1/TypeHandler1.java").getFile());
		File fr = new File(getClass().
				getResource("/examples/test1/TypeHandler2.java").getFile());
	
		CtDiff result = diff.analyze(fl,fr);
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
	
		CtDiff result = diff.analyze(fl,fr);
		List<Action> actions = result.getRootActions();
		assertEquals(actions.size(), 1);
		assertTrue(containsAction(actions, "UPD", "PAR-Literal"));
	
	}
	
	@Test
	public void exampleRemoveMethod() throws Exception{
		
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File(getClass().
				getResource("/examples/test3/CommandLine1.java").getFile());
		File fr = new File(getClass().
				getResource("/examples/test3/CommandLine2.java").getFile());
	
		CtDiff result = diff.analyze(fl,fr);
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
	
		CtDiff result = diff.analyze(fl,fr);
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
	//@Test
	public void testContent() throws IOException{
		File fl = new File(getClass().
				getResource("/examples/test4/CommandLine1.java").getFile());
		File fr = new File(getClass().
				getResource("/examples/test4/CommandLine2.java").getFile());
		DiffSpoon diff = new DiffSpoon(true);
		CtSimpleType ctl = diff.getSpoonType(diff.readFile(fl));
		//System.out.println();
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
}
