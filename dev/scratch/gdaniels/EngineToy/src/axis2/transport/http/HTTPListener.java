package axis2.transport.http;

import axis2.AxisEngine;
import axis2.Message;
import axis2.MessageContext;
import axis2.Responder;
import axis2.om.OMElement;
import test.ServiceDispatcher;

/**
 * A sample HTTP listener class
 */
public class HTTPListener implements HTTPConstants {
    AxisEngine engine;

    public void test(String msg) {
        MessageContext context = new MessageContext();
        context.setTransport("http");
        
        Message message = new Message();
        OMElement om = new OMElement();
        om.setObjectValue(msg);
        message.setContent(om);
        context.setMessage(message);

        // Tell the dispatcher what service name to use...
        context.setProperty(ServiceDispatcher.SERVICE_NAME, "AxisTestService");

        // Drop an appropriate responder in the MessageContext
        Responder responder = new HTTPResponder();
        context.setResponder(responder);

        boolean ret = false;
        try {
            ret = engine.receive(context);
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace();
        }

        if (ret == false) {
            System.out.println("MessageContext was paused - I can't deal with that!");
            return;
        }

        // If there was already a response, do nothing.  If not (as would be the case
        // for one-way messages or messages whose responses were routed elsewhere),
        // return a 202.
        if (!context.isPropertyTrue(RESPONSE_SENT)) {
            System.out.println("HTTPListener Sending 202");
        }
    }

    public void setEngine(AxisEngine engine) {
        this.engine = engine;
    }
}
