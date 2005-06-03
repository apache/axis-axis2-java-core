package userguide.clients;

import org.apache.axis.Constants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.clientapi.Call;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.io.StringWriter;

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


public class EchoClient {
    private static String IP="http://127.0.0.1:8080";
    private static EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO,
                    IP + "/axis2/services/MyService/echo");
      private static QName operationName = new QName("echo");
    private static String value;

    public static void main(String[] args) throws AxisFault {

        try {
            OMElement payload = ClientUtil.getEchoOMElement();
            Call call = new Call();
            call.setTo(targetEPR);
            call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);


            OMElement result = (OMElement) call.invokeBlocking(operationName.getLocalPart(),
                    payload);
            StringWriter writer = new StringWriter();
            result.serializeWithCache(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
            writer.flush();
             value= writer.toString();
            System.out.println(value);

        } catch (AxisFault axisFault) {
            value = axisFault.getMessage();

        } catch (XMLStreamException e) {
            value = e.getMessage();

        }

    }


}
