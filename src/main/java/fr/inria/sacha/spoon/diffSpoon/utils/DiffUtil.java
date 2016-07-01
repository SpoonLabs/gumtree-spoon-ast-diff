package fr.inria.sacha.spoon.diffSpoon.utils;

import spoon.compiler.SpoonCompiler;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.support.compiler.VirtualFile;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;
import spoon.support.compiler.jdt.JDTSnippetCompiler;

import java.io.File;
import java.util.List;

public final class DiffUtil {
	public static CtType getCtClass(Factory factory, File f) throws Exception {
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);
		compiler.getFactory().getEnvironment().setLevel("OFF");
		compiler.addInputSource(SpoonResourceHelper.createResource(f));
		compiler.build();
		return factory.Type().getAll().get(0);
	}

	public static void getCtClass(Factory factory, String contents) {
		SpoonCompiler builder = new JDTSnippetCompiler(factory, contents);
		try {
			builder.build();
		} catch (Exception e) {
			throw new RuntimeException("snippet compilation error while compiling: " + contents, e);
		}
	}

	public static CtType<?> getCtType(Factory factory, String content) {
		//Matias: using the Snippet compiler new JDTSnippetCompiler(factory, content);
		// only compiles private class and throws an exception.
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);
		compiler.addInputSource(new VirtualFile(content, "/test"));
		compiler.build();
		return factory.Type().getAll().get(0);
	}

	public static CtType getSpoonType(Factory factory, String contents) {
		try {
			getCtClass(factory, contents);
		} catch (Exception e) {
			// must fails
		}
		List<CtType<?>> types = factory.Type().getAll();
		if (types.isEmpty()) {
			throw new RuntimeException("No Type was created by spoon");
		}
		CtType spt = types.get(0);
		spt.getPackage().getTypes().remove(spt);

		return spt;
	}

	private DiffUtil() {
		throw new AssertionError("No instance.");
	}
}
