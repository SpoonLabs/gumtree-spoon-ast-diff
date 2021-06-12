package gumtree.spoon.builder;

import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.reflect.path.CtRole;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;

import static org.junit.Assert.assertEquals;

public class CtWrapperTest {
    @Test
    public void testRoleOfFinalInField() {
        // the role of final modifier in a field should be CtRole.IS_FINAL

        final Factory factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
        final CtClass<?> fieldClass = factory.Class().create("FieldClass");

        final CtField<String> field = factory.Core().createField();
        field.setType(factory.Type().STRING);
        field.setSimpleName("FIELD");

        CtWrapper<?> modifier = new CtWrapper<>(ModifierKind.FINAL, field);

        fieldClass.addField(field);

        assertEquals(CtRole.IS_FINAL, modifier.getRoleInParent());
    }
}
