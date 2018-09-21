package gumtree.spoon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.Test;

import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.Launcher;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.path.CtPath;
import spoon.reflect.visitor.CtScanner;

public class TreeTest {

	@Test
	public void test_Path_1() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astLeft = diff.getCtType(fl);

		assertNotNull(astLeft);
		assertEquals("QuickNotepad", astLeft.getSimpleName());

		CtPath pathLeft = astLeft.getPath();
		assertNotNull(pathLeft);

		CtType<?> astRight = diff.getCtType(fr);

		assertNotNull(astRight);
		assertEquals("QuickNotepad", astRight.getSimpleName());
		// No package
		assertEquals("QuickNotepad", astRight.getQualifiedName());

		CtPath pathRight = astRight.getPath();
		assertNotNull(pathRight);

		pathOfStmt(astLeft);
		pathOfStmt(astRight);

	}

	@Test
	public void test_Path_of_affected_nodes() throws Exception {
		AstComparator comparator = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astLeft = comparator.getCtType(fl);

		assertNotNull(astLeft);

		CtType<?> astRight = comparator.getCtType(fr);
		assertNotNull(astRight);

		Diff diffResult = comparator.compare(astLeft, astRight);
		System.out.println("Roots ops");
		List<Operation> rootOperations = diffResult.getRootOperations();
		getPaths(rootOperations);
		System.out.println("All ops");
		List<Operation> allOperations = diffResult.getAllOperations();
		getPaths(allOperations);

	}

	@Test
	public void test_Path_Scanner_failing_tofix() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astLeft = diff.getCtType(fl);

		assertNotNull(astLeft);
		assertEquals("QuickNotepad", astLeft.getSimpleName());

		CtPath pathLeft = astLeft.getPath();
		assertNotNull(pathLeft);

		CtType<?> astRight = diff.getCtType(fr);

		assertNotNull(astRight);
		assertEquals("QuickNotepad", astRight.getSimpleName());
		// No package in that file
		assertEquals("QuickNotepad", astRight.getQualifiedName());

		CtPath pathRight = astRight.getPath();
		assertNotNull(pathRight);

		PathScanner pscanner = new PathScanner();
		astLeft.accept(pscanner);
		astRight.accept(pscanner);

	}

	@Test
	public void test_Path_ScannerWithSpoon_left() throws Exception {

		Launcher spoon = new Launcher();
		Factory factory = spoon.createFactory();
		spoon.createCompiler(factory,
				SpoonResourceHelper.resources("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java"))
				.build();

		CtType<?> astLeft = factory.Type().get("QuickNotepad");
		assertNotNull(astLeft.getPath());
		assertNotNull(astLeft);
		PathScanner pscanner = new PathScanner();
		astLeft.accept(pscanner);

	}

	@Test
	public void test_Path_ScannerWithSpoon_Right() throws Exception {

		Launcher spoon = new Launcher();
		Factory factory = spoon.createFactory();
		spoon.createCompiler(factory,
				SpoonResourceHelper.resources("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java"))
				.build();

		CtType<?> astLeft = factory.Type().get("QuickNotepad");
		assertNotNull(astLeft.getPath());
		assertNotNull(astLeft);
		PathScanner pscanner = new PathScanner();
		astLeft.accept(pscanner);

	}

	@Test
	public void test_Path_ScannerWithGTFailing_left() throws Exception {

		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");

		CtType<?> astLeft = diff.getCtType(fl);

		PathScanner pscanner = new PathScanner();

		assertNotNull(astLeft.getPath());
		assertNotNull(astLeft);
		astLeft.accept(pscanner);

	}

	@Test
	public void test_Path_ScannerWithGTFailing_right() throws Exception {

		AstComparator diff = new AstComparator();

		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astRight = diff.getCtType(fr);

		PathScanner pscanner = new PathScanner();

		assertNotNull(astRight.getPath());
		assertNotNull(astRight);
		astRight.accept(pscanner);

	}

	@Test
	public void test_Path_ScannerWithGTFailing_both() throws Exception {

		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");

		CtType<?> astLeft = diff.getCtType(fl);

		File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

		CtType<?> astRight = diff.getCtType(fr);

		PathScanner pscanner = new PathScanner();

		assertNotNull(astLeft.getPath());
		assertNotNull(astLeft);
		astLeft.accept(pscanner);

		assertNotNull(astRight.getPath());
		assertNotNull(astRight);
		astRight.accept(pscanner);

	}

	@Test
	public void test_Path_ScannerWithSpoon_both() throws Exception {

		Launcher spoon = new Launcher();
		Factory factory = spoon.createFactory();
		spoon.createCompiler(factory,
				SpoonResourceHelper.resources("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java"))
				.build();

		CtType<?> astLeft = factory.Type().get("QuickNotepad");

		// So, if we create a new factory, it works.
		// Reusing the same factory, the path fails.
		factory = spoon.createFactory();
		spoon.createCompiler(factory,
				SpoonResourceHelper.resources("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java"))
				.build();

		CtType<?> astRight = factory.Type().get("QuickNotepad");

		PathScanner pscanner = new PathScanner();

		assertNotNull(astLeft.getPath());
		assertNotNull(astLeft);
		astLeft.accept(pscanner);

		assertNotNull(astRight.getPath());
		assertNotNull(astRight);
		astRight.accept(pscanner);

	}

	private void getPaths(List<Operation> rootops) {
		for (Operation<?> op : rootops) {

			CtElement left = op.getSrcNode();
			CtPath pleft = left.getPath();
			System.out.println(pleft);
			assertNotNull(pleft);

			CtElement right = op.getSrcNode();
			CtPath pright = right.getPath();
			System.out.println(pright);
			assertNotNull(pleft);

		}
	}

	private void pathOfStmt(CtType<?> ast) {
		for (CtMethod method : ast.getAllMethods()) {

			CtPath path = method.getPath();
			System.out.println("path " + path);
			assertNotNull(path);

			for (CtStatement stmt : method.getBody().getStatements()) {
				path = stmt.getPath();
				System.out.println("path " + path);
				assertNotNull(path);
			}

		}
	}

	private final class PathScanner extends CtScanner {
		@Override
		public void scan(CtElement element) {
			if (element != null) {
				System.out.println("Element: " + element.getShortRepresentation());
				CtPath path = element.getPath();
				System.out.println("Path: " + path.toString());
				assertNotNull(path);

			}
			// to avoid visiting all childs
			super.scan(element);
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void test_packageName_202564() throws Exception {
		AstComparator diff = new AstComparator();
		File fl = new File("src/test/resources/examples/t_202564/left_PropPanelModelElement_1.9.java");

		CtType typel = diff.getCtType(fl);
		assertNotNull(typel);
		assertEquals("org.argouml.uml.ui.foundation.core.PropPanelModelElement", typel.getQualifiedName());
		assertEquals("org.argouml.uml.ui.foundation.core", typel.getPackage().getQualifiedName());

	}

}
