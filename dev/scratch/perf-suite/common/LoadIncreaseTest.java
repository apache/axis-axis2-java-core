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
public class LoadIncreaseTest {
    private static int numberOfRequests = 100;
    private PrintWriter writer;
    private Sampler sampler;
    private String message;
    private int startTestAt = 10000;
    private int incrementTestBy = 1000;

    public LoadIncreaseTest(
        PrintWriter writer,
        Sampler sampler,
        String message) {
        this.sampler = sampler;
        this.writer = writer;
        this.message = message;
    }

    public void invokeTest() throws IOException {
        int count = startTestAt;
        while (true) {
            count = count + incrementTestBy;
            System.out.print("Invoke =" + count + " |");
            writer.write("Invoke =" + count + " |");
            try {
                sampler.init(count);
                sampler.invoke();
                sampler.end();
            } catch (Exception e) {
                System.out.println("Failed at the size " + count);
                writer.write("Failed at the size " + count);
                e.printStackTrace();
                e.printStackTrace(writer);
                break;
            }
        }
    }

}























