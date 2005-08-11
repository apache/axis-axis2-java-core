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

import org.apache.axis2.AxisFault;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.exception.XMLComparisonException;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
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
import java.util.Map;

public class MessageComparator {
    //public static final String TEST_MAIN_DIR = "./modules/samples/";
    public static final String TEST_MAIN_DIR = "./";
    private Log log = LogFactory.getLog(getClass());

    public boolean compare(String testNumber, InputStream replyMessage) {
        SOAPEnvelope replyMessageEnvelope;
        SOAPEnvelope requiredMessageEnvelope;
        try {
            File file = new File(TEST_MAIN_DIR+"test-resources/SOAP12Testing/ReplyMessages/SOAP12ResT" + testNumber + ".xml");

            HTTPTransportReceiver receiver = new HTTPTransportReceiver();
            //This step is needed to skip the headers :)
            receiver.parseTheHeaders(replyMessage, false);

            XMLStreamReader requiredMessageParser = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file));
            OMXMLParserWrapper requiredMessageBuilder = new StAXSOAPModelBuilder(requiredMessageParser,null);
            requiredMessageEnvelope = (SOAPEnvelope) requiredMessageBuilder.getDocumentElement();

            XMLStreamReader replyMessageParser = XMLInputFactory.newInstance().createXMLStreamReader(replyMessage);
            OMXMLParserWrapper replyMessageBuilder = new StAXSOAPModelBuilder(replyMessageParser,null);
            replyMessageEnvelope = (SOAPEnvelope) replyMessageBuilder.getDocumentElement();

            SOAPComparator soapComparator = new SOAPComparator();
            //ignore elements that belong to the addressing namespace
            soapComparator.addIgnorableNamespace("http://schemas.xmlsoap.org/ws/2004/08/addressing");
//            ////////////////////////////////////////////////////
            System.out.println("######################################################");
            OMOutputImpl omOutput = new OMOutputImpl(System.out,false);
            requiredMessageEnvelope.serializeWithCache(omOutput);
            omOutput.flush();
            System.out.println("");
            System.out.println("-------------------------------------------------------");
           OMOutputImpl omOutput1 = new OMOutputImpl(System.out,false);
            replyMessageEnvelope.serializeWithCache(omOutput1);
            omOutput1.flush();
            System.out.println("");
                   System.out.println("`######################################################");
            /////////////////////////////////////////////////////



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
