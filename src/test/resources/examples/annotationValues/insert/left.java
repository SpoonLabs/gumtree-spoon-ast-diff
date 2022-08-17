import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Column(id = 1, description = "blah")
public class AnnotationValue { }

@Target({ElementType.TYPE})
@interface Column {
    int id();
    String description();
    String type() default "String";
    String value() default "42"
}
