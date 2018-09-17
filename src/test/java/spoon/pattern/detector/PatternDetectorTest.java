package spoon.pattern.detector;

import org.junit.Test;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import spoon.pattern.detector.testclasses.Kopernik;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.testing.utils.ModelUtils;

public class PatternDetectorTest {

	@Test
	public void testVariableMethodNameAndLiteral() throws Exception {
		//contract: two methods with similar content, just different name and value in invocation
		CtType<?> type = ModelUtils.buildClass(Kopernik.class);
		
		CtMethod<?> method1 = type.getNestedType("A").getMethodsByName("mars").get(0);
		CtMethod<?> method2 = type.getNestedType("B").getMethodsByName("saturn").get(0);
		
		AstComparator diffComparator = new AstComparator(method1.getFactory());
		Diff diff = diffComparator.compare(method1, method2);
		System.out.println(diff.toString());
	}

	@Test
	public void testVariableMethodReturnValueAndReturnExpression() throws Exception {
		//contract: two methods with similar content, just different return value
		CtType<?> type = ModelUtils.buildClass(Kopernik.class);
		
		CtMethod<?> method1 = type.getNestedType("B").getMethodsByName("saturn").get(0);
		CtMethod<?> method2 = type.getNestedType("C").getMethodsByName("saturn").get(0);
		
		AstComparator diffComparator = new AstComparator(method1.getFactory());
		Diff diff = diffComparator.compare(method1, method2);
		System.out.println(diff.toString());
	}

	@Test
	public void testVariableMethodReturnValue() throws Exception {
		//contract: two methods with same content, just different return value
		CtType<?> type = ModelUtils.buildClass(Kopernik.class);
		
		CtMethod<?> method1 = type.getNestedType("D").getMethodsByName("saturn").get(0);
		CtMethod<?> method2 = type.getNestedType("E").getMethodsByName("saturn").get(0);
		
		AstComparator diffComparator = new AstComparator(method1.getFactory());
		Diff diff = diffComparator.compare(method1, method2);
		System.out.println(diff.toString());
	}
}
