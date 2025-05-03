class SpoonInvestigation {
    class HttpServlet{
	    public static int foo;
    }
    private void x(){
        int i;
        new HttpServlet(){};
    }
    public void y() {
        Object x = new HttpServlet(){
            protected void doGet(){
              this.foo = 3;
	    }
        };
    }
}
