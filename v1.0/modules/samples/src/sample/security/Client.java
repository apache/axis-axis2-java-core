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

package sample.security;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;

public class Client {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {

            // Get the repository location from the args
            String repo = args[0];
            String port = args[1];

            OMElement payload = getEchoElement();
            ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repo, null);
            ServiceClient serviceClient = new ServiceClient(configContext, null);
            Options options = new Options();
            options.setTo(new EndpointReference("http://127.0.0.1:" + port + "/axis2/services/SecureService"));
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            options.setAction("urn:echo");
            serviceClient.setOptions(options);
            
            //Blocking invocation
            OMElement result = serviceClient.sendReceive(payload);

            StringWriter writer = new StringWriter();
            result.serialize(XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(writer));
            writer.flush();

            System.out.println("Response: " + writer.toString());

            System.out.println("SecureService Invocation successful :-)");
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private static OMElement getEchoElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(
                "http://example1.org/example1", "example1");
        OMElement method = fac.createOMElement("echo", omNs);
        OMElement value = fac.createOMElement("Text", omNs);
        value.addChild(fac.createOMText(value, "Axis2 Echo String "));
        method.addChild(value);

        return method;
    }

}
