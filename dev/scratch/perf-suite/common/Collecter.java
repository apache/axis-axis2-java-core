import java.io.IOException;
import java.io.Writer;

/*
 * Created on Feb 10, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Collecter {
    private int waitingCount = 0;
    private int sucsess = 0;

    private static Writer writer = null;
    private long[] results;
    private int index = 0;
    private String message = null;

    public Collecter(int size, String message, Writer writer)
        throws IOException {
        this.message = message;
        Collecter.writer = writer;
        results = new long[size];
    }

    public void add(long value) {
        results[index] = value;
        index++;
    }

    public synchronized void waitForMe() {
        waitingCount++;

    }
    public synchronized void printResult() throws IOException {
        if (waitingCount > 1) {
            waitingCount--;
        } else {
            int failed = 0;
            long totel = 0;

            for (int i = 10; i < results.length; i++) {

                if (results[i] == -1) {
                    failed++;
                } else if (results[i] > 0) {
                    sucsess++;
                    totel = totel + results[1];
                }

            }
            
            int calls = failed + sucsess;
            double average = totel / (sucsess);
            String result = "["+message+"], sucsess = " + sucsess + "/"+ calls + " average ( "+totel+"/"+sucsess+" )= "+ average+"\n"; 
            System.out.println(result);
            writer.write(result);
            writer.flush();
        }
    }

}
