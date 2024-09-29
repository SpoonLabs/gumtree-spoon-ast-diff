package gumtree.spoon;

import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.DiffConfiguration;
import gumtree.spoon.diff.DiffImpl;
import spoon.SpoonModelBuilder;
import spoon.compiler.SpoonResource;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.VirtualFile;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import java.io.File;
import java.util.Map;

/**
 * Computes the differences between two CtElements.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 */
public class AstComparator {

	/**
	 * By default, comments are ignored
	 */
	private boolean includeComments = false;

	public AstComparator() {
		super();
	}

	public AstComparator(boolean includeComments) {
		super();
		this.includeComments = includeComments;
	}

	public AstComparator(Map<String, String> configuration) {
		super();
		for (String k : configuration.keySet()) {
			System.setProperty(k, configuration.get(k));
		}
	}

	private static String getFilename(String leftcontent) {
		return "test" + Math.abs(leftcontent.hashCode()) + ".java";
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: DiffSpoon <file_1>  <file_2>");
			return;
		}

		final Diff result = new AstComparator().compare(new File(args[0]), new File(args[1]));
		System.out.println(result.toString());
	}

	protected Factory createFactory() {
		Factory factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
		factory.getEnvironment().setNoClasspath(true);
		factory.getEnvironment().setCommentEnabled(includeComments);
		return factory;
	}

	/**
	 * compares two java files
	 */
	public Diff compare(File f1, File f2) throws Exception {
		CtPackage ctPackage1 = getCtPackage(f1);
		CtPackage ctPackage2 = getCtPackage(f2);
		if (ctPackage1 == null || ctPackage2 == null) {
			return null;
		} else {
			return compare(ctPackage1, ctPackage2);
		}
	}

	/**
	 * compares two AST nodes from two files according with a given configuration
	 */
	public Diff compare(File f1, File f2, DiffConfiguration configuration) throws Exception {
		final SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
		return new DiffImpl(
				scanner.getTreeContext(), scanner.getTree(getCtPackage(f1)), scanner.getTree(getCtPackage(f2)), configuration);
	}

	/**
	 * compares two snippets
	 */
	public Diff compare(String left, String right) {
		return compare(getCtPackage(left, getFilename(left)), getCtPackage(right, getFilename(right)));
	}

	public CtPackage getCtPackage(String content, String filename) {
		VirtualFile resource = new VirtualFile(content, filename);
		return getCtPackage(resource);
	}

	/**
	 * compares two snippets
	 */
	public Diff compare(String left, String right, DiffConfiguration configuration) {
		return compare(getCtPackage(left, getFilename(left)), getCtPackage(right, getFilename(right)), configuration);
	}

	/**
	 * compares two snippets that come from the files given as argument
	 */
	public Diff compare(String left, String right, String filenameLeft, String filenameRight) {
		return compare(getCtPackage(left, filenameLeft), getCtPackage(right, filenameRight));
	}

	/**
	 * compares two AST nodes
	 */
	public Diff compare(CtElement left, CtElement right) {
		final SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
		return new DiffImpl(scanner.getTreeContext(), scanner.getTree(left), scanner.getTree(right));
	}

	/**
	 * compares two AST nodes according with a given configuration
	 */
	public Diff compare(CtElement left, CtElement right, DiffConfiguration configuration) {
		final SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
		return new DiffImpl(scanner.getTreeContext(), scanner.getTree(left), scanner.getTree(right), configuration);
	}

	public CtType getCtType(File file) throws Exception {

		SpoonResource resource = SpoonResourceHelper.createResource(file);
		return getCtType(resource);
	}

	public CtPackage getCtPackage(File file) throws Exception {
		SpoonResource resource = SpoonResourceHelper.createResource(file);
		return getCtPackage(resource);
	}

	public CtType getCtType(SpoonResource resource) {
		Factory factory = createFactory();
		factory.getModel().setBuildModelIsFinished(false);
		SpoonModelBuilder compiler = new JDTBasedSpoonCompiler(factory);
		compiler.getFactory().getEnvironment().setLevel("OFF");
		compiler.addInputSource(resource);
		compiler.build();

		if (factory.Type().getAll().size() == 0) {
			return null;
		}

		// let's first take the first type.
		CtType type = factory.Type().getAll().get(0);
		// Now, let's ask to the factory the type (which it will set up the
		// corresponding
		// package)
		return factory.Type().get(type.getQualifiedName());
	}

	public CtPackage getCtPackage(SpoonResource resource) {
		Factory factory = createFactory();
		factory.getModel().setBuildModelIsFinished(false);
		SpoonModelBuilder compiler = new JDTBasedSpoonCompiler(factory);
		compiler.getFactory().getEnvironment().setLevel("OFF");
		compiler.addInputSource(resource);
		compiler.build();
		return factory.Package().getRootPackage();
	}

	public CtType<?> getCtType(String content) {
		return getCtType(content, "/test");
	}

	public CtType<?> getCtType(String content, String filename) {
		VirtualFile resource = new VirtualFile(content, filename);
		return getCtType(resource);
	}
}
