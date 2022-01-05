package gumtree.spoon.diff;

import static org.junit.Assert.assertEquals;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.UpdateOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import spoon.reflect.path.CtRole;

import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class UpdateOperationTest {
    @Parameterized.Parameters(name = "{3}")
    public static Collection<Object[]> provideTestStringsAndExpectedRole() {
        return Arrays.asList(new Object[][] {
                {
                    "class A { public void B() { System.out.println(1+1) } }",
                    "class A { public void B() { System.out.println(2+1) } }",
                    CtRole.VALUE,
                    "Left operand (CtLiteral)",
                },
                {
                    "class A { public void B() { System.out.println(1+1) } }",
                    "class A { public void B() { System.out.println(1-1) } }",
                    CtRole.OPERATOR_KIND,
                    "Operator kind (CtBinaryOperator)",
                },
                {
                    "class A { public void B() { System.exit() } }",
                    "class A { public void B() { System.out.println(\"Nothing.\") } }",
                    CtRole.EXECUTABLE_REF,
                    "Executable (CtExecutableRef)",
                },
                {
                    "class A { }",
                    "class B { }",
                    CtRole.NAME,
                    "Name of class",
                },
                {
                    "class A { void add() { } }",
                    "class A { void sub() { } }",
                    CtRole.NAME,
                    "Name of method",
                },
                {
                    "class A { void add() { } }",
                    "class A { int add() { } }",
                    CtRole.NAME,
                    "Return type of method"
                },
                {
                    "public class A { }",
                    "private class A { }",
                    CtRole.MODIFIER,
                    "Access modifier",
                },
                {
                    "public class A { void x() throws IOException { } }",
                    "public class A { void x() throws SQLException { } }",
                    CtRole.NAME,
                    "Thrown type",
                },
        });
    }

    private final String left;
    private final String right;
    private final CtRole expectedRole;
    private final String testMessage;

    public UpdateOperationTest(String left, String right, CtRole expectedRole, String testMessage) {
        this.left = left;
        this.right = right;
        this.expectedRole = expectedRole;
        this.testMessage = testMessage;
    }

    @Test
    public void test_roleOfUpdatedNode() {
        Diff diff = new AstComparator().compare(left, right);
        assertEquals(1, diff.getUpdateOperations().size());

        UpdateOperation updateOperation = (UpdateOperation) diff.getUpdateOperations().get(0);
        assertEquals(expectedRole, updateOperation.getUpdatedRole());
    }
}
