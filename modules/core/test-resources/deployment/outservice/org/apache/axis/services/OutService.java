package org.apache.axis2.services;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Mar 16, 2005
 * Time: 3:56:50 PM
 */

public class OutService extends Thread{
    protected Log log = LogFactory.getLog(getClass());

    public void run() {
        while(true){
            sentOutMessage();
        }
    }

    private void sentOutMessage() {
        SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = omFactory.getDefaultEnvelope();
        OMNode testNode = omFactory.createText("This is auto generated messge by the server at " + new Date());
        env.getBody().addChild(testNode);
        log.info("Generated Message" + env.getBody().getFirstChild());

        /*EndpointReference targetEPR = null;
        String action = null;
        Writer out = null;
        try {
        final AxisEngine engine = new AxisEngine();
        MessageContext msgctx = new MessageContext(registry, null, null,Utils.createHTTPTransport(registry));
        msgctx.setEnvelope(env);
        msgctx.setTo(targetEPR);

        msgctx.setTo(targetEPR);
        if (action != null) {
        msgctx.setProperty(MessageContext.SOAP_ACTION, action);
        }
        engine.send(msgctx);
        } catch (IOException e) {
        throw AxisFault.makeFault(e);
        } finally {
        try {
        out.close();
        } catch (IOException e1) {
        throw new AxisFault();
        }
        }
*/
    }
}
