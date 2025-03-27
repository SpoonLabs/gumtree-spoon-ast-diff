public class SpoonInvestigation {
    private void x(){
        sleep(1);
    }
    public void y() {
        startServer(new HttpServlet(){
            @Override
            protected void doGet(){}
        });
    }
}