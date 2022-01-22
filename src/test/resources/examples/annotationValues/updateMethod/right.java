import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@interface Column {
    String b();
}

@Column(b = "1")
class A { }
