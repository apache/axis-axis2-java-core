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

package sample.mtom.interop.client;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import java.io.File;


public class InteropClientModel {
    private File inputFile = null;

    private EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:8080/axis2/services/interopService");

    private QName operationName = new QName("mtomSample");


    public InteropClientModel() {

    }

    private OMElement createEnvelope(String fileName) throws Exception {

        DataHandler expectedDH;
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://example.org/mtom/data", "x");
        OMElement data = fac.createOMElement("Data", omNs);

        File dataFile = new File(fileName);
        FileDataSource dataSource = new FileDataSource(dataFile);
        expectedDH = new DataHandler(dataSource);
        OMText textData = fac.createOMText(expectedDH, true);
        data.addChild(textData);
        return data;
    }

    public OMElement testEchoXMLSync(String fileName) throws Exception {

        OMElement payload = createEnvelope(fileName);
        Options options = new Options();

        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTo(targetEPR);
        // enabling MTOM in the client side
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_FALSE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ServiceClient sender = new ServiceClient();
        sender.setOptions(options);
        return sender.sendReceive(payload);
    }

    public void setTargetEPR(String targetEPR) {
        this.targetEPR = new EndpointReference(targetEPR);
    }
}
