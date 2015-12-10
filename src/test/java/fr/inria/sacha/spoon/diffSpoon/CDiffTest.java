package fr.inria.sacha.spoon.diffSpoon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

/**
 * Test Spoon Diff 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class CDiffTest {

	private final String newline = System.getProperty("line.separator");

	
	@Test
	public void testToString() throws Exception {
		File fl = new File("src/test/resources/examples/test1/TypeHandler1.java");
		File fr = new File("src/test/resources/examples/test1/TypeHandler2.java");

		DiffSpoonImpl diff = new DiffSpoonImpl();
		CtDiffImpl result = diff.compare(fl,fr);
		assertEquals("Update FieldRead at org.apache.commons.cli.TypeHandler:80" +newline
				+ "\t(org.apache.commons.cli.PatternOptionBuilder.DATE_VALUE) to (org.apache.commons.cli.PatternOptionBuilder.CLASS_VALUE)" + newline
				+ "Insert Invocation at org.apache.commons.cli.TypeHandler:118" + newline
				+ "\tjava.lang.System.out.println(\"Hola\")" + newline, result.toString());

		fl = new File("src/test/resources/examples/test2/CommandLine1.java");
		fr = new File("src/test/resources/examples/test2/CommandLine2.java");

		result = diff.compare(fl,fr);
		assertEquals("Update Literal at org.apache.commons.cli.CommandLine:275" + newline
				+ "\t1 to 1000000" + newline, result.toString());

		fl = new File("src/test/resources/examples/test3/CommandLine1.java");
		fr = new File("src/test/resources/examples/test3/CommandLine2.java");

		result = diff.compare(fl,fr);
		assertTrue(result.toString().endsWith("Delete Method at org.apache.commons.cli.CommandLine:161" + newline
				+ "\tpublic java.lang.String[] getOptionValues(java.lang.String opt) {" + newline
				+ "\t    java.util.List<java.lang.String> values = new java.util.ArrayList<java.lang.String>();" + newline
				+ "\t    for (org.apache.commons.cli.Option option : options) {" + newline
				+ "\t        if ((opt.equals(option.getOpt())) || (opt.equals(option.getLongOpt()))) {" + newline
				+ "\t            values.addAll(option.getValuesList());" + newline
				+ "\t        } " + newline
				+ "\t    }" + newline
				+ "\t    return values.isEmpty() ? null : values.toArray(new java.lang.String[values.size()]);" + newline
				+ "\t}" + newline));
	}
}
