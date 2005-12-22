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
package userguide.clients;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;

import java.io.StringWriter;

/**
 * To run this sample you have to deploy WsaMappingService.aar to the
 * service folder.
 */
public class EchoBlockingWsaBasedClient {

    private static EndpointReference targetEPR = new EndpointReference("http://localhost:8080/axis2/services/WsaMappingTest");

    private static OMElement getBody() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac
                .createOMNamespace("http://example1.org/example1", "example1");
        OMElement id = fac.createOMElement("id", omNs);
        id.addChild(fac.createText(id, "Axis2"));
        return id;
    }

    public static void main(String[] args) throws Exception {

        Call call = new Call();
        Options options = new Options();
        call.setClientOptions(options);
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        //Blocking invocation via wsa mapping
        options.setAction("urn:sample/echo");
        OMElement result = (OMElement) call.invokeBlocking("echo", getBody());

        StringWriter writer = new StringWriter();
        result.serialize(writer);
        writer.flush();

        System.out.println(writer.toString());

    }
}
