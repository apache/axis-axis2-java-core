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
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class SOAPCreater {

    private static final Log log = LogFactory.getLog(SOAPCreater.class);

    public String getStringFromSOAPMessage(String testNumber, URL url) throws IOException {
        File file =
                new File(
                        MessageComparator.TEST_MAIN_DIR +
                                "test-resources/SOAP12Testing/RequestMessages/SOAP12ReqT" +
                                testNumber + ".xml");
        FileInputStream stream = new FileInputStream(file);
        StringBuffer sb = new StringBuffer();

        sb.append(HTTPConstants.HEADER_POST).append(" ");
        sb.append(url.getFile()).append(" ").append(HTTPConstants.HEADER_PROTOCOL_10).append("\n");
        sb.append(HTTPConstants.HEADER_CONTENT_TYPE).append(": ").append(
                SOAP12Constants.SOAP_12_CONTENT_TYPE);
        sb.append("; charset=utf-8\n");
        sb.append("\n");

        String record;
        BufferedReader reder = new BufferedReader(new InputStreamReader(stream));
        while ((record = reder.readLine()) != null) {
            sb.append(record.trim());
        }
        return sb.toString();
    }

    public SOAPEnvelope getEnvelopeFromSOAPMessage(String pathAndFileName) {
        File file = new File(pathAndFileName);
        try {
            XMLStreamReader parser =
                    StAXUtils.createXMLStreamReader(new FileReader(file));
            OMXMLParserWrapper builder = new StAXSOAPModelBuilder(parser, null);
            return (SOAPEnvelope)builder.getDocumentElement();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
