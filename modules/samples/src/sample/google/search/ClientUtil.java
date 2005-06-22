/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.google.search;

import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.context.MessageContext;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;

/**
 * Builds the MessageContext as called by AsynchronousClient
 * First build the request soap envilope
 * then build a messageContext and soap envelope is attached to it
 *
 * @author Gayan Asanka  (gayan@opensource.lk)
 */
public class ClientUtil {

    /** Soap request is included to this and pass it to sendMsg() in AsynchronousClient */
    //static MessageContext msgContext;

    /**
     * method getMessageContext
     * @return msgContext
     */
    public static MessageContext getMessageContext(AsynchronousClient asyncClient) throws DeploymentException {
        OMNamespace defNs;
        OMElement operation;
        MessageContext msgContext = null;

        String str_ST_index = Integer.toString(asyncClient.getStartIndex());

        defNs = OMAbstractFactory.getSOAP11Factory().createOMNamespace("", "");
        SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = omFactory.getDefaultEnvelope();
        envelope.declareNamespace(
                "http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV");
        envelope.declareNamespace(
                "http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        envelope.declareNamespace(
                "http://www.w3.org/1999/XMLSchema-instance/", "xsi");
        envelope.declareNamespace("http://www.w3.org/1999/XMLSchema",
                "xsd");

        operation = omFactory.createOMElement("doGoogleSearch", "urn:GoogleSearch", "ns1");
        envelope.getBody().addChild(operation);
        operation.addAttribute("SOAP-ENV:encordingStyle",
                "http://schemas.xmlsoap.org/soap/encoding/", null);

        operation.addChild(getOMElement(omFactory,defNs,"oe","xsd:string","latin1"));
        operation.addChild(getOMElement(omFactory,defNs,"ie","xsd:string","latin1"));
        operation.addChild(getOMElement(omFactory,defNs,"lr","xsd:string",""));
        operation.addChild(getOMElement(omFactory,defNs,"safeSearch","xsd:boolean","false"));
        operation.addChild(getOMElement(omFactory,defNs,"restrict","xsd:string",""));
        operation.addChild(getOMElement(omFactory,defNs,"filter","xsd:boolean","true"));
        operation.addChild(getOMElement(omFactory,defNs,"maxResults","xsd:int","10"));
        operation.addChild(getOMElement(omFactory,defNs,"start","xsd:int",str_ST_index));
        operation.addChild(getOMElement(omFactory,defNs,"q","xsd:string",asyncClient.getSearch()));
        operation.addChild(getOMElement(omFactory,defNs,"key","xsd:string",asyncClient.getKey()));

        ConfigurationContextFactory fac = new ConfigurationContextFactory();
        ConfigurationContext configContext = fac.buildClientConfigurationContext("doGoogleSearch");
        try {
            msgContext = new MessageContext(configContext);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        msgContext.setEnvelope(envelope);
        return msgContext;
    }

    private static OMElement getOMElement(OMFactory factory,OMNamespace ns,String elementName,String type,String text){
        OMElement part = factory.createOMElement(elementName, ns);
        part.addAttribute("xsi:type", type, null);
        part.addChild(factory.createText(text));
        return part;
    }
}



