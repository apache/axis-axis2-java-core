package userguide.clients;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.MessageSender;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Jun 3, 2005
 * Time: 2:11:25 PM
 */
public class PingClient {
    private static EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO,
            "http://127.0.0.1:8080/axis2/services/MyService/ping");

    public static void main(String[] args) {
        try {
            OMElement payload = ClientUtil.getPingOMElement();

            MessageSender msgSender = new MessageSender();
            msgSender.setTo(targetEPR);
            msgSender.setSenderTransport(Constants.TRANSPORT_HTTP);

            msgSender.send("ping", payload);

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

}
