package userguide.clients;

import org.apache.axis2.Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.MessageSender;
import org.apache.axis2.om.OMElement;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Jun 3, 2005
 * Time: 2:11:25 PM
 */
public class PingClient {
    private static EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:8080/axis2/services/MyService/ping");

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
