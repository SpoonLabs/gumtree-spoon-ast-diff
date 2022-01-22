import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@interface Column {
    String a();
}

@Column(a = "1")
class A { }
