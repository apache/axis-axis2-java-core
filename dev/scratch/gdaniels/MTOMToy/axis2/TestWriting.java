package axis2;

import axis2.om.OMElement;
import axis2.samples.BaseWriter;
import axis2.samples.MTOMWriter;

import javax.activation.DataHandler;

/**
 * Simple test of MTOM-like writing.  Create a toy OM hierarchy:
 * <root>
 *   <child>{text content}</child>
 * </root>
 *
 * Serialize the exact same OMElement with a BaseWriter and an
 * MTOMWriter, to demonstrate the basic flow.
 */
public class TestWriting {
    public static void main(String[] args) throws Exception {
        SerializationContext sc = new BasicSerializationContext();
        BaseWriter bw = new BaseWriter(System.out, sc);
        OMElement root = new OMElement("root");
        OMElement child = new OMElement("child");

        DataHandler dh = new DataHandler("Hello there!", "text/plain");
        child.setContent(dh);
        root.addChild(child);
        root.writeTo(bw);

        MTOMWriter mw = new MTOMWriter(System.out, sc);
        root.writeTo(mw);
    }
}
