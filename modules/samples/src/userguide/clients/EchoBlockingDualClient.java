package userguide.clients;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.impl.llom.OMOutputer;

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
    private static EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO,
            "http://127.0.0.1:8080/axis2/services/MyService/echo");

    public static void main(String[] args) {
        try {
            OMElement payload = ClientUtil.getEchoOMElement();

            Call call = new Call();
            call.setTo(targetEPR);

            //The boolean flag informs the axis2 engine to use two separate transport connection
            //to retrieve the response.
            call.engageModule(new QName(Constants.MODULE_ADDRESSING));
            call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, true);

            //Blocking Invocation
            OMElement result = (OMElement) call.invokeBlocking("echo", payload);

            StringWriter writer = new StringWriter();
            result.serializeWithCache(new OMOutputer(XMLOutputFactory.newInstance().createXMLStreamWriter(writer)));
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
