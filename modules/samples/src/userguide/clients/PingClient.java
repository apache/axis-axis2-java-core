package userguide.clients;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.clientapi.Call;
import org.apache.axis.clientapi.MessageSender;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.Constants;
import org.apache.axis.om.OMElement;
import org.apache.axis.soap.SOAPEnvelope;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.namespace.QName;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Jun 3, 2005
 * Time: 2:11:25 PM
 */
public class PingClient {
      private static String IP="http://127.0.0.1:8080";
    private static EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO,
                    IP + "/axis2/services/MyService/echo");
      private static QName operationName = new QName("echo");
    private static String value;
    public static void main(String[] args) throws AxisFault{

          try {
            OMElement payload = ClientUtil.getPingOMElement();

              MessageSender msgSender= new MessageSender();
              msgSender.setTo(targetEPR);
              msgSender.setSenderTransport(Constants.TRANSPORT_HTTP);

              msgSender.send("ping",payload);



        } catch (AxisFault axisFault) {
            value = axisFault.getMessage();

        }
    }


}
