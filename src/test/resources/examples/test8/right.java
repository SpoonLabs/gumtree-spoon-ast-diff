public class TestMain {
    static public void main() throws IOException {
        try{
            TestClass.testMethod(1);
        }catch (NullPointerException e){
            e.printStackTrace();
          
        }
    }
}
