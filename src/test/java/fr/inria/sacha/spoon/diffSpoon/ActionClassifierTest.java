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
		DiffSpoonImpl diff = new DiffSpoonImpl();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");
		CtDiffImpl result = diff.compare(fl,fr);
		
		List<Action> actionsRoot = result.getRootActions();
		result.debugInformation();
		assertEquals(1, actionsRoot.size());
		assertTrue(result.containsAction( "Insert", "If"));
	}
	/**
	 * changes an element and adds a new child to it
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_2() throws Exception{
		DiffSpoonImpl diff = new DiffSpoonImpl();
		File fl = new File("src/test/resources/examples/roots/test9/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test9/right_QuickNotepad_1.14.java");
		CtDiffImpl result = diff.compare(fl,fr);
		
		List<Action> actionsRoot = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actionsRoot.size());
		
		assertTrue(result.containsAction("Update", "BinaryOperator"));
		assertTrue(result.containsAction("Insert", "Return"));
	}

	/**
	 * Remove element, keep its only child
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_3() throws Exception{
		DiffSpoonImpl diff = new DiffSpoonImpl();
		File fl = new File("src/test/resources/examples/roots/test10/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test10/right_QuickNotepad_1.14.java");
		CtDiffImpl result = diff.compare(fl,fr);
		
		List<Action> actionsRoot = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actionsRoot.size());
		
		assertTrue(result.containsAction("Delete", "If"));
		assertTrue(result.containsAction("Move", "Invocation"));
	}
	
	/**
	 * Removes element with 2 children, keep one child, remove the other
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_4() throws Exception{
		DiffSpoonImpl diff = new DiffSpoonImpl();
		File fl = new File("src/test/resources/examples/roots/test11/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test11/right_QuickNotepad_1.14.java");
		CtDiffImpl result = diff.compare(fl,fr);
		
		List<Action> actionsRoot = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actionsRoot.size());
		
		assertTrue(result.containsAction("Delete", "If"));
		assertTrue(result.containsAction("Move", "Invocation"));
	}
	
	/**
	 * Add element with 2 children, one new, other from a move
	 * @throws Exception
	 */
	@Test
	public void test_actionClassifier_5() throws Exception{
		DiffSpoonImpl diff = new DiffSpoonImpl();
		File fl = new File("src/test/resources/examples/roots/test12/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test12/right_QuickNotepad_1.14.java");
		CtDiffImpl result = diff.compare(fl,fr);
		
		List<Action> actionsRoot = result.getRootActions();
		result.debugInformation();
		assertEquals(2, actionsRoot.size());
		
		assertTrue(result.containsAction("Insert", "If"));
		assertTrue(result.containsAction("Move", "Invocation"));
	}
}
