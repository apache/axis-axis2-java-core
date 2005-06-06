package userguide.clients;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.AsyncResult;
import org.apache.axis.clientapi.Call;
import org.apache.axis.clientapi.Callback;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Jun 4, 2005
 * Time: 5:08:44 PM
 */
public class EchoNonBlockingClient {
    private static EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO,
            "http://127.0.0.1:8080/axis2/services/MyService/echo");

    public static void main(String[] args) {
        try {
            OMElement payload = ClientUtil.getEchoOMElement();

            Call call = new Call();
            call.setTo(targetEPR);
            call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);

            //Callback to handle the response
            Callback callback = new Callback() {
                public void onComplete(AsyncResult result) {
                    try {
                        StringWriter writer = new StringWriter();
                        result.getResponseEnvelope().serializeWithCache(
                                XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
                        writer.flush();

                        System.out.println(writer.toString());

                    } catch (XMLStreamException e) {
                        reportError(e);
                    }
                }

                public void reportError(Exception e) {
                    e.printStackTrace();
                }
            };

            //Non-Blocking Invocation
            call.invokeNonBlocking("echo", payload, callback);

            //Wait till the callback receives the response.
            while (!callback.isComplete()) {
                Thread.sleep(1000);
            }

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
