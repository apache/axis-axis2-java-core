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
public class LoadTest implements Runnable {
    private int sizeOfArray = 10;
    private int numberOfRequests = 0;
    private int no;
    private PrintWriter writer;
    private Sampler sampler;
    private String message;
    private Collecter c;

    public LoadTest(
        int no,
        PrintWriter writer,
        Sampler sampler,
        String message,
        Collecter c,
        int numberOfRequests) {
        this.sampler = sampler;
        this.writer = writer;
        this.message = message;
        this.c = c;
        this.no = no;
        this.numberOfRequests = numberOfRequests;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        c.waitForMe();
        try {
            for (int i = 0; i < numberOfRequests; i++) {
                try {
                    sampler.init(sizeOfArray);
                    long start = System.currentTimeMillis();
                    sampler.invoke();
                    long end = System.currentTimeMillis();
                    sampler.end();
                    long time = end - start;
                    c.add(time);
                    System.out.println(
                        "Thread" + no + " request " + i + " = " + time);
                } catch (Exception e) {
                    e.printStackTrace();
                    e.printStackTrace(writer);
                    c.add(-1);
                    writer.flush();
                }
            }

        } finally {
            try {
                c.printResult();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Thread " + no + " stop");
    }

}
