import java.io.IOException;
import java.io.PrintWriter;

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
public class EndToEndTest {
    private static int numberOfRequests = 100;
//    private int no;
    private PrintWriter writer; 
    private Sampler sampler; 
    private String message;


    public EndToEndTest(PrintWriter writer,Sampler sampler,String message){
//        this.no = no;
        this.sampler = sampler;
        this.writer = writer;
        this.message = message;
    }
    
    public void invokeTest() throws IOException{
        int[] vals = new int[]{1, 10,100,500};
        
        for (int j = 0; j < vals.length; j++) {
            Collecter c = new Collecter(100,message + "Array Size =" + vals[j], writer);
            for (int i = 0; i < numberOfRequests; i++) {
                try {
                    sampler.init(vals[j]);
                    long start = System.currentTimeMillis();
                    sampler.invoke();
                    long end = System.currentTimeMillis();
                    sampler.end();
                    long time = end - start;
                    c.add(time);
                    System.out.println(i + " = " + time);
                } catch (Exception e) {
                    e.printStackTrace();
                    e.printStackTrace(writer);
                    c.add(-1);
                }
            }
            c.printResult();
        }
    }
}
