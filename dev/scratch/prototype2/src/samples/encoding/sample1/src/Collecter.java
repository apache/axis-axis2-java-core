import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/*
 * Created on Feb 10, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Collecter {
    private static Writer writer = null;
    private long[] results;
    private int index = 0;
    private String message = null;

    public Collecter(int size, String message, Writer writer) throws IOException {
        this.message = message;
        Collecter.writer = writer;
        results = new long[size];
    }

    public void add(long value) {
        results[index] = value;
        index++;
    }

    public void printResult() throws IOException {
        int failed = 0;
        long totel = 0;

        for (int i = 0; i < results.length; i++) {

            if (results[i] <= 0) {
                failed++;
            } else {
                totel = totel + results[1];
            }

        }
        System.out.println("Calls =" + results.length);
        System.out.println("Faliure =" + failed);
        System.out.println("Average =" + totel / (results.length - failed));
        writer.write("--------------------------------------");
        writer.write(message + "\n");
        writer.write("Average =" + totel / (results.length - failed)+"\n");
        writer.write("--------------------------------------");
        writer.flush();
    }

}
