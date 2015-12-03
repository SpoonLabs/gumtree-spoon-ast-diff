package fr.inria.sacha.spoon.diffSpoon;

import java.io.File;

import spoon.reflect.declaration.CtElement;

/**
 * Computes the differences between two CtElements.
 */
public interface DiffSpoon {

	/** compares two java files */
	public CtDiffImpl compare(File f1, File f2) throws Exception;

	/** compares two AST nodes */
	public CtDiffImpl compare(CtElement left, CtElement right);

	/** says that the dependencies are not in the classpath (noclasspath mode of Spoon) */
	public void setNoClasspath(boolean noClasspath);

}