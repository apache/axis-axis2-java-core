import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

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
    private static int sizeOfArray = 10;
    private static int numberOfRequests = 100;
    private int no;
    private static int THREAD_COUNT = 100;
    private Collecter c; 
    public EndToEndTest(int no,Collecter c){
        this.no = no;
    }

    public static void main(String[] args) throws IOException {
        
        Writer writer = new FileWriter("results/results.txt");
        writer.write("Axis2 Perf\n#########################\n");
        int[] vals = new int[]{1, 10,100,1000};
        for (int j = 0; j < vals.length; j++) {
            Collecter c = new Collecter(100,"Array Size =" + vals[j],writer);
            for (int i = 0; i < numberOfRequests; i++) {
                try {
                    Sampler sampler = new Sampler(vals[j], c);
                    System.out.print(i + " = ");
                    sampler.invokeService();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            c.printResult();
        }
        writer.write("#########################\n");
        writer.close();

    }
}
