package userguide.clients;

import org.apache.axis2.Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Jun 4, 2005
 * Time: 5:47:37 PM
 */
public class EchoBlockingDualClient {
    private static EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:8080/axis2/services/MyService/echo");

    public static void main(String[] args) {
        try {
            OMElement payload = ClientUtil.getEchoOMElement();

            Call call = new Call();
            call.setTo(targetEPR);

            //The boolean flag informs the axis2 engine to use two separate transport connection
            //to retrieve the response.
            call.engageModule(new QName(Constants.MODULE_ADDRESSING));
            call.setTransportInfo(Constants.TRANSPORT_HTTP,
                    Constants.TRANSPORT_HTTP,
                    true);

            //Blocking Invocation
            OMElement result = (OMElement) call.invokeBlocking("echo",
                    payload);

            StringWriter writer = new StringWriter();
            result.serializeWithCache(XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(writer));
            writer.flush();

            System.out.println(writer.toString());

            //Need to close the Client Side Listener.
            call.close();

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
