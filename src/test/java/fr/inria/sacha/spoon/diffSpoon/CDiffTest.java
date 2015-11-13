package fr.inria.sacha.spoon.diffSpoon;

import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Test Spoon Diff 
 * @author  Matias Martinez, matias.martinez@inria.fr
 *
 */
public class CDiffTest {

	private final String newline = System.getProperty("line.separator");

	
	@Test
	public void testToString() throws Exception {
		URL fl = getClass().getResource("/examples/test1/TypeHandler1.java");
		URL fr = getClass().getResource("/examples/test1/TypeHandler2.java");

		DiffSpoon diff = new DiffSpoon(true);
		CtDiff result = diff.compare(fl,fr);
		assertEquals("Update FieldRead at org.apache.commons.cli.TypeHandler:80" +newline
				+ "\t(org.apache.commons.cli.PatternOptionBuilder.DATE_VALUE) to (org.apache.commons.cli.PatternOptionBuilder.CLASS_VALUE)" + newline
				+ "Insert Invocation at org.apache.commons.cli.TypeHandler:118" + newline
				+ "\tjava.lang.System.out.println(\"Hola\")" + newline, result.toString());

		fl = getClass().getResource("/examples/test2/CommandLine1.java");
		fr =getClass().getResource("/examples/test2/CommandLine2.java");

		result = diff.compare(fl,fr);
		assertEquals("Update Literal at org.apache.commons.cli.CommandLine:275" + newline
				+ "\t1 to 1000000" + newline, result.toString());

		fl = getClass().getResource("/examples/test3/CommandLine1.java");
		fr =getClass().getResource("/examples/test3/CommandLine2.java");

		result = diff.compare(fl,fr);
		assertEquals("Delete Method at org.apache.commons.cli.CommandLine:168" + newline
				+ "\tpublic java.lang.String[] getOptionValues(java.lang.String opt) {" + newline
				+ "\t    java.util.List<java.lang.String> values = new java.util.ArrayList<java.lang.String>();" + newline
				+ "\t    for (org.apache.commons.cli.Option option : options) {" + newline
				+ "\t        if ((opt.equals(option.getOpt())) || (opt.equals(option.getLongOpt()))) {" + newline
				+ "\t            values.addAll(option.getValuesList());" + newline
				+ "\t        } " + newline
				+ "\t    }" + newline
				+ "\t    return values.isEmpty() ? null : values.toArray(new java.lang.String[values.size()]);" + newline
				+ "\t}" + newline, result.toString());
	}
}
