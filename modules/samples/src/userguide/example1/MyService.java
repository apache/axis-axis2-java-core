package userguide.example1;

import org.apache.axis.om.OMElement;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Jun 2, 2005
 * Time: 2:17:58 PM
 */
public class MyService {
    public OMElement echo(OMElement element) throws XMLStreamException {

        StringWriter writer = new StringWriter();
        element.serializeWithCache(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
        writer.flush();

        System.out.println(writer.toString());

        element.getNextSibling();
        element.detach();
        return element;
    }

    public void ping(OMElement element) {
        //Do the ping
    }
}
