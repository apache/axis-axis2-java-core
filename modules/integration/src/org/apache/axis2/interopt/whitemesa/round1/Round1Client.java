package org.apache.axis2.interopt.whitemesa.round1;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.interopt.whitemesa.round1.util.Round1ClientUtil;
import org.apache.axis2.soap.SOAPEnvelope;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;


public class Round1Client {

    public SOAPEnvelope sendMsg(Round1ClientUtil util, String epUrl, String soapAction) throws AxisFault {

        SOAPEnvelope retEnv = null;
        URL url = null;
        try {
            url = new URL(epUrl);
        } catch (MalformedURLException e) {
            throw new AxisFault(e);
        }

        Call call = new Call();
        call.setTo(new EndpointReference(url.toString()));
        call.setSoapAction(soapAction);
        call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
        SOAPEnvelope reqEnv = util.getEchoSoapEnvelope();

        System.out.println("");

        AxisConfiguration axisConfig = new AxisConfigurationImpl();
        ConfigurationContext configCtx = new ConfigurationContext(axisConfig);
        MessageContext msgCtx = new MessageContext(configCtx);
        msgCtx.setEnvelope(reqEnv);


        QName opName = new QName("");
        OperationDescription opDesc = new OperationDescription(opName);
        MessageContext retMsgCtx = call.invokeBlocking(opDesc, msgCtx);
        //SOAPEnvelope responseEnvelop = replyContext.getEnvelope();
        retEnv = retMsgCtx.getEnvelope();

        return retEnv;
    }
}


