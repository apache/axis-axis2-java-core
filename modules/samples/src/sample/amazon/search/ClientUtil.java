/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package sample.amazon.search;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.DeploymentException;

/**
 * Builds the MessageContext as called by AsynchronousClient
 * First build the request soap envilope
 * then build a messageContext and soap envelope is attached to it
 *
 * @auther Gayan Asanka  (gayan@opensource.lk)
 */
public class ClientUtil {

    /**
     * Soap request is included to this and pass it to sendMsg() in AsynchronousClient
     */
    private static MessageContext msgContext;

    /**
     * method getMessageContext
     *
     * @return msgContext
     */
    public static MessageContext getMessageContext() throws AxisFault {
        OMNamespace namespace, nulNS;
        OMElement operation, value1, value2;
        OMElement subValue1, subValue2, subValue3, subValue4, subValue5;

        SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omFactory.getDefaultEnvelope();
        namespace = reqEnv.declareNamespace(
                "http://schemas.xmlsoap.org/soap/envelope/",
                "SOAP-ENV");
        namespace = reqEnv.declareNamespace(
                "http://schemas.xmlsoap.org/soap/encoding/",
                "SOAP-ENC");
        namespace =
                reqEnv.declareNamespace(
                        "http://www.w3.org/1999/XMLSchema-instance/", "xsi");
        namespace =
                reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema",
                        "xsd");
        namespace = reqEnv.declareNamespace(
                "http://schemas.xmlsoap.org/wsdl/soap/",
                "soap");
        namespace = reqEnv.declareNamespace("http://schemas.xmlsoap.org/wsdl/",
                "wsdl");
        namespace = reqEnv.declareNamespace(
                "http://webservices.amazon.com/AWSAlexa/2005-02-01",
                "tns");

        nulNS = omFactory.createOMNamespace("", "");

        operation =
                omFactory.createOMElement("Search",
                        "http://webservices.amazon.com/AWSAlexa/2005-02-01",
                        "ns1");
        reqEnv.getBody().addChild(operation);
        operation.addAttribute("encordingStyle",
                "http://schemas.xmlsoap.org/soap/encoding/",
                null);


        value1 = omFactory.createOMElement("SubscriptionId", nulNS);
        value1.addChild(omFactory.createOMText(AsynchronousClient.amazonkey));
        //this is a valid sample key :- "0Y6WJGPB6TW8AVAHGFR2"));

        value2 = omFactory.createOMElement("Request", nulNS);

        subValue1 = omFactory.createOMElement("ResponseGroup", nulNS);
        subValue1.addChild(omFactory.createOMText("Web"));

        subValue2 = omFactory.createOMElement("Query", nulNS);
        subValue2.addChild(omFactory.createOMText(AsynchronousClient.search));

        subValue3 = omFactory.createOMElement("Count", nulNS);
        subValue3.addChild(omFactory.createOMText(AsynchronousClient.maxResults));

        subValue4 = omFactory.createOMElement("IgnoreWords", nulNS);
        subValue4.addChild(omFactory.createOMText("90"));

        subValue5 = omFactory.createOMElement("AdultFilter", nulNS);
        subValue5.addChild(omFactory.createOMText("yes"));

        value2.addChild(subValue5);
        value2.addChild(subValue4);
        value2.addChild(subValue3);
        value2.addChild(subValue2);
        value2.addChild(subValue1);
        operation.addChild(value2);
        operation.addChild(value1);

        ConfigurationContext configContext = null;
        try {
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem("Search","Search/axis2.xml");
        } catch (DeploymentException e) {
            e.printStackTrace();
        }
        msgContext = new MessageContext();
        msgContext.setConfigurationContext(configContext);
        msgContext.setEnvelope(reqEnv);
        return msgContext;
    }


}



