/*
 * Created on Feb 9, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**
 * @author hemapani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class LoadTest implements Runnable {
    private int sizeOfArray = 10;
    private int numberOfRequests = 100;
    private int no;
    
    public LoadTest(int no){
        this.no = no;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println("Thread "+i+ " started");
            Thread thread = new Thread(new LoadTest(i));
            thread.start();
        }
    }
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        
        for (int i = 0; i < numberOfRequests; i++) {
            
            try {
                Sampler sampler = new Sampler(sizeOfArray);
                sampler.invokeService();
            } catch (Exception e) {
                e.printStackTrace();
            }
            

        }
        System.out.println("Thread "+no+ " stop");
    }

}
