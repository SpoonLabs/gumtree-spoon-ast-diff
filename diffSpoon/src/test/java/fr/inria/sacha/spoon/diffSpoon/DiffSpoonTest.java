package fr.inria.sacha.spoon.diffSpoon;

import static org.junit.Assert.*;

import org.junit.Test;
/**
 * 
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

	//@Test
	public void testAnalyzeCtElementCtElement() {
		//fail("Not yet implemented");
	}

}
