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

package test.soap12testing.client;

import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.impl.exception.XMLComparisonException;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPTransportReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;

public class MessageComparator {
    //public static final String TEST_MAIN_DIR = "./modules/samples/";
    public static final String TEST_MAIN_DIR = "./";
	private static final Log log = LogFactory.getLog(MessageComparator.class);

    public boolean compare(String testNumber, InputStream replyMessage) {
        SOAPEnvelope replyMessageEnvelope;
        SOAPEnvelope requiredMessageEnvelope;
        try {
            File file = new File(TEST_MAIN_DIR+"test-resources/SOAP12Testing/ReplyMessages/SOAP12ResT" + testNumber + ".xml");

            HTTPTransportReceiver receiver = new HTTPTransportReceiver();
            //This step is needed to skip the headers :)
            receiver.parseTheHeaders(replyMessage, false);

            XMLStreamReader requiredMessageParser = StAXUtils.createXMLStreamReader(new FileReader(file));
            OMXMLParserWrapper requiredMessageBuilder = new StAXSOAPModelBuilder(requiredMessageParser,null);
            requiredMessageEnvelope = (SOAPEnvelope) requiredMessageBuilder.getDocumentElement();

            XMLStreamReader replyMessageParser = StAXUtils.createXMLStreamReader(replyMessage);
            OMXMLParserWrapper replyMessageBuilder = new StAXSOAPModelBuilder(replyMessageParser,null);
            replyMessageEnvelope = (SOAPEnvelope) replyMessageBuilder.getDocumentElement();

            SOAPComparator soapComparator = new SOAPComparator();
            //ignore elements that belong to the addressing namespace
            soapComparator.addIgnorableNamespace("http://schemas.xmlsoap.org/ws/2004/08/addressing");

            System.out.println("######################################################");
            requiredMessageEnvelope.serialize(System.out);
            System.out.println("");
            System.out.println("-------------------------------------------------------");
            replyMessageEnvelope.serialize(System.out);
            System.out.println("");
                   System.out.println("`######################################################");

            return soapComparator.compare(requiredMessageEnvelope,replyMessageEnvelope);

        } catch (XMLStreamException e) {
            log.info(e.getMessage());
        } catch (FileNotFoundException e) {
            log.info(e.getMessage());
        } catch (XMLComparisonException e) {
            log.info(e.getMessage());
        } catch (AxisFault axisFault) {
            log.info(axisFault.getMessage());
        }
        return false;
    }
}
