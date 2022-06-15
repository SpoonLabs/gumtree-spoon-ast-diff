import java.util.HashMap;
import java.util.Map;

public class GenericType {
    public static void main(String[] args) {
        Map<String, String> m = new HashMap<>();
        m.put("1", "1");
        System.out.println(((Map<String, String>) m));
    }
}
