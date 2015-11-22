package fr.inria.sacha.spoon.diffSpoon;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.github.gumtreediff.actions.model.Action;
/**
 * Test the action classifier, which creates the RootAction list from CtDiff. 
 * 
 *
 */
public class ActionClassifierTest {
	
	/**
	 * Add new element + child
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_1() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actionsRoot = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actionsRoot.size());
		
		List<Action> actionsAll = result.getAllActions();
		assertTrue(actionsAll.size() > 1);
		assertTrue(diff.containsAction(actionsAll, "Insert", "Invocation", "println"));
	}
	/**
	 * changes an element and adds a new child to it
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_2() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/roots/test9/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test9/right_QuickNotepad_1.14.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actionsRoot = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actionsRoot.size());
		
		assertTrue(diff.containsAction(actionsRoot , "Update", "BinaryOperator"));
		assertTrue(diff.containsAction(actionsRoot , "Insert", "Return"));
	}

	/**
	 * Remove element, keep its only child
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_3() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/roots/test10/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test10/right_QuickNotepad_1.14.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actionsRoot = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actionsRoot.size());
		
		assertTrue(diff.containsAction(actionsRoot , "Delete", "If"));
		assertTrue(diff.containsAction(actionsRoot , "Move", "Invocation"));
	}
	
	/**
	 * Removes element with 2 children, keep one child, remove the other
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_4() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/roots/test11/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test11/right_QuickNotepad_1.14.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actionsRoot = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actionsRoot.size());
		
		assertTrue(diff.containsAction(actionsRoot , "Delete", "If"));
		assertTrue(diff.containsAction(actionsRoot , "Move", "Invocation"));
	}
	
	/**
	 * Add element with 2 children, one new, other from a move
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_5() throws Exception{
		DiffSpoon diff = new DiffSpoon(true);
		File fl = new File("src/test/resources/examples/roots/test12/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test12/right_QuickNotepad_1.14.java");
		CtDiff result = diff.compare(fl,fr);
		
		List<Action> actionsRoot = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actionsRoot.size());
		
		assertTrue(diff.containsAction(actionsRoot , "Insert", "If"));
		assertTrue(diff.containsAction(actionsRoot , "Move", "Invocation"));
	}
}
