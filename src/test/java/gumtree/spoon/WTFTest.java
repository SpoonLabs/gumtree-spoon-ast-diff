package gumtree.spoon;

import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WTFTest {
	// Table of behaviour:
	//                     | TestWTF executed ALONE | Executing test suite
	// test2 COMMENTED     |  FAILING               |  FAILING
	// test2 NOT COMMENTED |  PASSING               |  FAILING

	@Test
	public void testWTF() throws Exception{
		AstComparator diff = new AstComparator();
		// meld  src/test/resources/examples/t_225008/left_Similarity_1.9.java src/test/resources/examples/t_225008/right_Similarity_1.10.java
		File fl = new File("src/test/resources/examples/t_225008/left_Similarity_1.9.java");
		File fr = new File("src/test/resources/examples/t_225008/right_Similarity_1.10.java");
		Diff result = diff.compare(fl,fr);

		List<Operation> actions = result.getRootOperations();
		result.debugInformation();
		assertEquals(1, actions.size());
		assertTrue(result.containsOperation(OperationKind.Update, "Modifier", "protected"));
	}


	@Test
	public void test2() {
		assertEquals(1, 1);
	}

	@Test
	public void test3() {
		assertEquals(1, 1);
	}

	@Test
	public void test4() {
		assertEquals(1, 1);
	}

	@Test
	public void test5() {
		assertEquals(1, 1);
	}

	@Test
	public void test6() {
		assertEquals(1, 1);
	}

	@Test
	public void test7() {
		assertEquals(1, 1);
	}

	@Test
	public void test8() {
		assertEquals(1, 1);
	}

	@Test
	public void test9() {
		assertEquals(1, 1);
	}

	@Test
	public void test10() {
		assertEquals(1, 1);
	}

	@Test
	public void test11() {
		assertEquals(1, 1);
	}

	@Test
	public void test12() {
		assertEquals(1, 1);
	}

	@Test
	public void test13() {
		assertEquals(1, 1);
	}

	@Test
	public void test14() {
		assertEquals(1, 1);
	}

	@Test
	public void test15() {
		assertEquals(1, 1);
	}
}
