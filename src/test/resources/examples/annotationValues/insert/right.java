import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Column(id = 1, type = "int", description = "blah", value = "41")
public class AnnotationValue { }

@Target({ElementType.TYPE})
@interface Column {
    int id();
    String description();
    String type() default "String";
    String value() default "42"
}
