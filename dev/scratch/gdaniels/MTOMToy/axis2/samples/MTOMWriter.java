package axis2.samples;

import axis2.SerializationContext;

import javax.activation.DataHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * MTOMWriters understand how to write MTOM packages.
 */
public class MTOMWriter extends BaseWriter {
    Map parts = new HashMap();
    OutputStream os;

    byte [] HEADER = "---PACKAGE HEADER---\n".getBytes();
    byte [] BEGINPART = "---START PART '".getBytes();
    byte [] ENDLINE = "'---\n".getBytes();
    byte [] ENDPART = "---END PART '".getBytes();
    byte [] FOOTER = "---PACKAGE FOOTER---\n".getBytes();

    public MTOMWriter(OutputStream os, SerializationContext serContext) {
        super(os, serContext);

        // Hold onto this
        this.os = os;

        try {
            // Write MIME packaging
            os.write(HEADER);
            // Start main part
            os.write(BEGINPART);
            os.write("XML".getBytes());
            os.write(ENDLINE);
        } catch (IOException e) {
            // deal with exception...
        }
    }

    public void writeBinary(DataHandler dh) throws Exception {
        String key = (String)parts.get(dh);
        if (key == null) {
            // Haven't yet written this
            key = "Attachment" + Integer.toString(parts.size());
            parts.put(dh, key);
        }

        startElement("mtom:include");
        writeText(key);
        endElement();
    }

    public void endDocument() throws Exception {
        flush();

        // End of XML, write closure for this part
        os.write(ENDPART);
        os.write("XML".getBytes());
        os.write(ENDLINE);

        // Now serialize each attachment
        for (Iterator i = parts.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();
            String partName = (String) entry.getValue();
            os.write(BEGINPART);
            os.write(partName.getBytes());
            os.write(ENDLINE);

            DataHandler dh = (DataHandler)entry.getKey();
            dh.writeTo(os);

            os.write(ENDPART);
            os.write(partName.getBytes());
            os.write(ENDLINE);
        }

        os.write(FOOTER);
        os.close();
    }
}
