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
import org.apache.axiom.om.impl.exception.XMLComparisonException;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class MessageComparator {
    //public static final String TEST_MAIN_DIR = "./modules/samples/";
    public static final String TEST_MAIN_DIR = "./";
    private static final Log log = LogFactory.getLog(MessageComparator.class);

    public boolean compare(String testNumber, InputStream replyMessage) {
        SOAPEnvelope replyMessageEnvelope;
        SOAPEnvelope requiredMessageEnvelope;
        try {
            File file = new File(TEST_MAIN_DIR +
                    "test-resources/SOAP12Testing/ReplyMessages/SOAP12ResT" + testNumber + ".xml");

            //This step is needed to skip the headers :)
            parseTheHeaders(replyMessage, false);

            XMLStreamReader requiredMessageParser =
                    StAXUtils.createXMLStreamReader(new FileReader(file));
            OMXMLParserWrapper requiredMessageBuilder =
                    new StAXSOAPModelBuilder(requiredMessageParser, null);
            requiredMessageEnvelope = (SOAPEnvelope)requiredMessageBuilder.getDocumentElement();

            XMLStreamReader replyMessageParser = StAXUtils.createXMLStreamReader(replyMessage);
            OMXMLParserWrapper replyMessageBuilder =
                    new StAXSOAPModelBuilder(replyMessageParser, null);
            replyMessageEnvelope = (SOAPEnvelope)replyMessageBuilder.getDocumentElement();

            SOAPComparator soapComparator = new SOAPComparator();
            //ignore elements that belong to the addressing namespace
            soapComparator
                    .addIgnorableNamespace("http://schemas.xmlsoap.org/ws/2004/08/addressing");

            System.out.println("######################################################");
            requiredMessageEnvelope.serialize(System.out);
            System.out.println("");
            System.out.println("-------------------------------------------------------");
            replyMessageEnvelope.serialize(System.out);
            System.out.println("");
            System.out.println("`######################################################");

            return soapComparator.compare(requiredMessageEnvelope, replyMessageEnvelope);

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


    /** Field BEFORE_SEPARATOR */
    private static final int BEFORE_SEPARATOR = 3;

    /** Field AFTER_SEPARATOR */
    private static final int AFTER_SEPARATOR = 4;

    /** Field lastRead */
    private int lastRead = -1;

    /** Field index */
    int index = 0;

    /** Field buf */
    private byte[] buf = new byte[1024];

    /** Field length */
    int length = 0;

    /** Field done */
    private boolean done = false;

    /**
     * Parses following two styles of HTTP stuff Server Side POST /axis2/services/echo HTTP/1.0
     * Content-Type: text/xml; charset=utf-8 Accept: application/soap+xml, application/dime,
     * multipart/related, text User-Agent: Axis/1.2RC1 Host: 127.0.0.1:8081 Cache-Control: no-cache
     * Pragma: no-cache SOAPAction: "" Content-Length: 73507 HTTP/1.1 200 OK Content-Type:
     * text/xml;charset=utf-8 Date: Sat, 12 Feb 2005 10:39:39 GMT Server: Apache-Coyote/1.1
     * Connection: close
     *
     * @param in
     * @param serverSide
     * @return Returns HashMap.
     * @throws AxisFault
     */
    public HashMap parseTheHeaders(InputStream in, boolean serverSide) throws AxisFault {
        HashMap map = new HashMap();

        try {
            StringBuffer str = new StringBuffer();
            int state = BEFORE_SEPARATOR;
            String key = null;
            String value = null;

            length = readLine(in, buf);

            if (serverSide) {
                if ((buf[0] == 'P') && (buf[1] == 'O') && (buf[2] == 'S') && (buf[3] == 'T')) {
                    map.put(HTTPConstants.HTTP_REQ_TYPE, HTTPConstants.HEADER_POST);
                    index = 5;
                } else if ((buf[0] == 'G') && (buf[1] == 'E') && (buf[2] == 'T')) {
                    map.put(HTTPConstants.HTTP_REQ_TYPE, HTTPConstants.HEADER_GET);
                    index = 4;
                } else {
                    throw new AxisFault(
                            "Unsupported HTTP request type: Only GET and POST is supported");
                }

                value = readFirstLineArg(' ');
                map.put(HTTPConstants.REQUEST_URI, value);
                value = readFirstLineArg('\n');
                map.put(HTTPConstants.PROTOCOL_VERSION, value);
            } else {
                index = 0;
                value = readFirstLineArg(' ');

                if ((value != null) && (value.indexOf("HTTP") >= 0)) {
                    map.put(HTTPConstants.PROTOCOL_VERSION, value);
                    value = readFirstLineArg(' ');
                    map.put(HTTPConstants.RESPONSE_CODE, value);
                } else {
                    map.put(HTTPConstants.RESPONSE_CODE, value);
                }

                value = readFirstLineArg('\n');
                map.put(HTTPConstants.RESPONSE_WORD, value);
            }

            state = BEFORE_SEPARATOR;

            while (!done) {
                length = readLine(in, buf);

                if (length <= 0) {
                    throw new AxisFault(Messages.getMessage("preatureEOS"));
                }

                for (int i = 0; i < length; i++) {
                    switch (state) {
                        case BEFORE_SEPARATOR :
                            if (buf[i] == ':') {
                                key = str.toString();
                                str = new StringBuffer();
                                state = AFTER_SEPARATOR;

                                if (buf[i + 1] == ' ') {
                                    i++;    // ignore next space
                                }
                            } else {
                                str.append((char)buf[i]);
                            }

                            break;

                        case AFTER_SEPARATOR :
                            if (buf[i] == '\n') {
                                value = str.toString();
                                map.put(key, value);
                                str = new StringBuffer();
                                i = length;
                            } else {
                                str.append((char)buf[i]);
                            }

                            break;

                        default :
                            throw new AxisFault("Error Occured Unknown state " + state);
                    }
                }

                state = BEFORE_SEPARATOR;
            }
        } catch (IOException e) {
            throw new AxisFault(e.getMessage(), e);
        }

        return map;
    }

    /**
     * Method readFirstLineArg.
     *
     * @param terminal
     * @return Returns String.
     * @throws org.apache.axis2.AxisFault
     */
    private String readFirstLineArg(char terminal) throws AxisFault {
        StringBuffer str = new StringBuffer();

        try {
            while ((buf[index] != terminal) && (index < length)) {
                str.append((char)buf[index]);
                index++;
            }

            index++;

            return str.toString();
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * Reads a single line from the input stream.
     *
     * @param is inputstream to read from
     * @param b  byte array to read into
     * @return Returns int.
     * @throws java.io.IOException
     */
    protected int readLine(InputStream is, byte[] b) throws java.io.IOException {
        int count = 0, c;

        if (lastRead == -1) {
            c = is.read();
        } else {
            c = lastRead;
        }

        int off = 0;

        while (c != -1) {
            if ((c != '\n') && (c != '\r')) {
                b[off++] = (byte)c;
                count++;
                c = is.read();
            } else {
                if ('\n' == c) {
                    c = is.read();

                    if (c == '\r') {
                        c = is.read();
                    }

                    // If the next line begins with tab or space then this is a continuation.
                    if ((c != ' ') && (c != '\t')) {
                        if (c == '\n') {
                            done = true;
                        }

                        lastRead = c;
                        b[off++] = '\n';
                        count++;

                        break;
                    }
                } else {
                    c = is.read();
                }
            }
        }

        if (c == -1) {
            throw new AxisFault("Every line should ends with the \\n, unexpected End of stream");
        } else {
            return (count > 0)
                    ? count
                    : -1;
        }
    }

}
