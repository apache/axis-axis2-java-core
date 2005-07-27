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

package org.apache.axis2.soap12testing.client;

import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.exception.XMLComparisonException;
import org.apache.axis2.transport.http.HTTPTransportReceiver;
import org.apache.axis2.engine.AxisFault;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.Map;

public class MessageComparator {
    public boolean compare(String testNumber, InputStream replyMessage) {
        SOAPEnvelope replyMessageEnvelope;
        SOAPEnvelope requiredMessageEnvelope;
        try {
//            File file = new File("D:\\Projects\\LSF\\Axis2\\Axis1.0\\modules\\samples/test-resources\\SOAP12Testing\\ReplyMessages\\SOAP12ResT" + testNumber + ".xml");
            File file = new File("test-resources\\SOAP12Testing\\ReplyMessages\\SOAP12ResT" + testNumber + ".xml");

            HTTPTransportReceiver receiver = new HTTPTransportReceiver();
            Map map = receiver.parseTheHeaders(replyMessage, false);




            XMLStreamReader requiredMessageParser = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file));
            OMXMLParserWrapper requiredMessageBuilder = new StAXSOAPModelBuilder(requiredMessageParser,null);
            requiredMessageEnvelope = (SOAPEnvelope) requiredMessageBuilder.getDocumentElement();

            XMLStreamReader replyMessageParser = XMLInputFactory.newInstance().createXMLStreamReader(replyMessage);
            OMXMLParserWrapper replyMessageBuilder = new StAXSOAPModelBuilder(replyMessageParser,null);
            replyMessageEnvelope = (SOAPEnvelope) replyMessageBuilder.getDocumentElement();

            SOAPComparator soapComparator = new SOAPComparator();
            return soapComparator.compare(requiredMessageEnvelope,replyMessageEnvelope);

        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLComparisonException e) {
            e.printStackTrace();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        return false;
    }
}
