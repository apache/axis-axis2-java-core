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
package org.apache.axis.transport;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.soap.impl.llom.soap11.SOAP11Factory;


public class TransportUtils {
    public static SOAPEnvelope createSOAPMessage(MessageContext msgContext)
        throws AxisFault {

        InputStream inStream =
            (InputStream) msgContext.getProperty(MessageContext.TRANSPORT_IN);
        msgContext.setProperty(MessageContext.TRANSPORT_IN, null);
        return createSOAPMessage(msgContext, inStream);
    }


 
    public static SOAPEnvelope createSOAPMessage(
        MessageContext msgContext,
        InputStream inStream )
        throws AxisFault {
        try {
            //Check for the REST behaviour, if you desire rest beahaviour
            //put a <parameter name="doREST" value="true"/> at the server.xml/client.xml file
            Object doREST =
                msgContext.getProperty(Constants.Configuration.DO_REST);
            Reader reader = new InputStreamReader(inStream);
            
            XMLStreamReader xmlreader =
                XMLInputFactory.newInstance().createXMLStreamReader(reader);
                
            StAXBuilder builder = null;
            SOAPEnvelope envelope = null;
            if (doREST != null && "true".equals(doREST)) {
                SOAPFactory soapFactory = new SOAP11Factory();
                builder = new StAXOMBuilder(xmlreader);
                builder.setOmbuilderFactory(soapFactory);
                envelope = soapFactory.getDefaultEnvelope();
                envelope.getBody().addChild(builder.getDocumentElement());
            } else {
                builder = new StAXSOAPModelBuilder(xmlreader);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            }
            return envelope;
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(), e);
        }
    }

}
