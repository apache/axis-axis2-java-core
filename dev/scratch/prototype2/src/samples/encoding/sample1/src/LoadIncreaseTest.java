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
public class LoadIncreaseTest {
    public static void main(String[] args) throws IOException {
        Writer writer = new FileWriter("results/result.txt");
        Collecter c = new Collecter(1000,"Load increase test, Axis2",writer);
        int count = 14000;
        while(true) {
            count = count + 1000;
            System.out.print("Invoke ="+ count + " |");
            
            Sampler sampler = new Sampler(count,c);
            try {
                sampler.invokeService();
            } catch (Exception e) {
                System.out.println("Failed at the size " + count);
                e.printStackTrace();
                break;
            }
        }
        c.printResult();
    }
}
