import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;

/*
 * Created on Feb 11, 2005
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
public class AxisTest {

    public static void main(String[] args) throws IOException {
        Writer writer = new FileWriter("result/results.txt",true);
        PrintWriter pw = new PrintWriter(writer);
        StructArraySampler sampler = new StructArraySampler(new URL(Constants.AXIS2_URL));
        TestSuite suite = new TestSuite(sampler,pw,"[Client=\"Axis1.2-RC3\" Server=\"Axis2\"] EchoStruct test");
        suite.runSuite();

        sampler = new StructArraySampler(new URL(Constants.AXIS_URL));
        suite = new TestSuite(sampler,pw,"[Client=\"Axis1.2-RC3\" Server=\"Axis1.2-RC3\"] EchoStruct test");
        suite.runSuite();

        writer.close();
    }
}
