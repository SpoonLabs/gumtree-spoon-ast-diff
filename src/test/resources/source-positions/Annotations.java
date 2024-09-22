import org.junit.Ignore;

import java.beans.JavaBean;

@Ignore("This is a test class")
@JavaBean(description = "This is a test class", defaultProperty = "name")
public class Annotations {
}