import java.io.IOException;

public class ThrownType {
    public void iThrow(String[] args) throws IndexOutOfBoundsException, IOException {
        String s = args[42];
        throw new IOException();
    }
}