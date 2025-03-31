public class SpoonInvestigation {
    private void x(){
        sleep(1);
        startServer(new HttpServlet(){});
    }
    public void y() {
        startServer(new HttpServlet(){
            @Override
            protected void doGet(){}
        });
    }
}