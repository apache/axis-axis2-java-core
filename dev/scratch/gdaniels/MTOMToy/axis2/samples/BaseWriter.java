package axis2.samples;

import axis2.SerializationContext;
import axis2.om.OMWriter;
import org.apache.axis.utils.XMLUtils;

import javax.activation.DataHandler;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Stack;

/**
 * Basic XML writing implementation, which writes base64 for binary content
 */
public class BaseWriter implements OMWriter {
    PrintWriter writer;
    Stack openElements = new Stack();
    SerializationContext serContext;

    public BaseWriter(OutputStream os, SerializationContext serContext) {
        this.serContext = serContext;
        writer = new PrintWriter(os);
    }

    public void startElement(String name) {
        writer.write("<");
        writer.write(name);
        writer.write(">");
        openElements.push(name);
    }

    public void endElement() throws Exception {
        String name = (String)openElements.pop();
        writer.write("</");
        writer.write(name);
        writer.write(">");
        if (openElements.isEmpty()) {
            endDocument();
        }
    }

    public void writeText(String text) {
        writer.write(text);
    }

    /**
     * Write object content (inside a tag).  If the content is
     * binary (i.e. a DataHandler in this example), use the
     * writeBinary() method.  If not, use the SerializationContext
     * to do the work.
     *
     * @param obj
     * @throws Exception
     */
    public void serialize(Object obj) throws Exception {
        if (obj instanceof DataHandler) {
            DataHandler dh = (DataHandler)obj;
            writeBinary(dh);
            return;
        }
        serContext.serialize(obj, this);
    }

    public void writeBinary(DataHandler dh) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dh.writeTo(baos);
        baos.close();
        writeText(XMLUtils.base64encode(baos.toByteArray()));
    }

    public void endDocument() throws Exception {
        flush();
    }

    public void flush() {
        writer.flush();
    }
}
