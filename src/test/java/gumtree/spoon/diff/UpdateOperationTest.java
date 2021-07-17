package gumtree.spoon.diff;

import static org.junit.Assert.assertEquals;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.UpdateOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import spoon.reflect.path.CtRole;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class UpdateOperationTest {
    @Parameters(name = "{2}")
    public static Collection<Object[]> provideTestStringsAndExpectedRole() {
        return Arrays.asList(new Object[][] {
                {
                        "class A { public void B() { System.out.println(1+1) } }",
                        "class A { public void B() { System.out.println(2+1) } }",
                        CtRole.VALUE,
                },
                {
                        "class A { public void B() { System.out.println(1+1) } }",
                        "class A { public void B() { System.out.println(1-1) } }",
                        CtRole.OPERATOR_KIND,
                },
                {
                        "class A { public void B() { System.exit() } }",
                        "class A { public void B() { System.out.println(\"Nothing.\") } }",
                        CtRole.EXECUTABLE_REF,
                },
                {
                        "class A { }",
                        "class B { }",
                        CtRole.NAME,
                },
                {
                        "class A { void add() { } }",
                        "class A { void sub() { } }",
                        CtRole.NAME,
                },
                {
                        "class A { void add() { } }",
                        "class A { int add() { } }",
                        CtRole.NAME,
                },
                {
                        "public class A { }",
                        "private class A { }",
                        CtRole.MODIFIER,
                },
        });
    }

    private final String left;
    private final String right;
    private final CtRole expectedRole;

    public UpdateOperationTest(String left, String right, CtRole expectedRole) {
        this.left = left;
        this.right = right;
        this.expectedRole = expectedRole;
    }

    @Test
    public void test_roleOfUpdatedNode() {
        Diff diff = new AstComparator().compare(left, right);
        assertEquals(1, diff.getUpdateOperations().size());

        UpdateOperation updateOperation = (UpdateOperation) diff.getUpdateOperations().get(0);
        assertEquals(expectedRole, updateOperation.getRole());
    }
}
