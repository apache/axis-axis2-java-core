package userguide.clients;

import org.apache.axis2.Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.AsyncResult;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.clientapi.Callback;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Jun 4, 2005
 * Time: 5:30:52 PM
 */
public class EchoNonBlockingDualClient {
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

            //Callback to handle the response
            Callback callback = new Callback() {
                public void onComplete(AsyncResult result) {
                    try {
                        StringWriter writer = new StringWriter();
                        result.getResponseEnvelope().serializeWithCache(XMLOutputFactory.newInstance()
                                .createXMLStreamWriter(writer));
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
            //Need to close the Client Side Listener.
            call.close();

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
