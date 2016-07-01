package fr.inria.sacha.spoon.diffSpoon;

import spoon.reflect.declaration.CtElement;

import java.io.File;

/**
 * Computes the differences between two CtElements.
 */
public interface DiffSpoon {

	/** compares two java files */
	CtDiff compare(File f1, File f2) throws Exception;

	/** compares two snippet */
	CtDiff compare(String left, String right);

	/** compares two AST nodes */
	CtDiff compare(CtElement left, CtElement right);

}