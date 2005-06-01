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
package org.apache.axis.transport.http;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis.util.Utils;

/**
 * Class HTTPTransportReceiver
 */
public class HTTPTransportReceiver {
    /**
     * Field END
     */
    private static final int END = 1;

    /**
     * Field END_OF_LINE
     */
    private static final int END_OF_LINE = 2;

    /**
     * Field BEFORE_SEPERATOR
     */
    private static final int BEFORE_SEPERATOR = 3;

    /**
     * Field AFTER_SEPERATOR
     */
    private static final int AFTER_SEPERATOR = 4;

    /**
     * Field lastRead
     */
    private int lastRead = -1;

    /**
     * Field buf
     */
    private char[] buf = new char[1024];

    /**
     * Field index
     */
    int index = 0;

    /**
     * Field length
     */
    int length = 0;

    /**
     * Field done
     */
    private boolean done = false;

    /**
     * Method invoke
     *
     * @param msgContext
     * @throws AxisFault
     */
    public SOAPEnvelope checkForMessage(
        MessageContext msgContext,
        ConfigurationContext engineContext)
        throws AxisFault {
        SOAPEnvelope soapEnvelope = null;

        Reader in =
            (Reader) msgContext.getProperty(MessageContext.TRANSPORT_READER);
        if (in != null) {
            boolean serverSide = msgContext.isServerSide();
            Map map = parseTheHeaders(in, serverSide);
            if (serverSide) {
                msgContext.setWSAAction(
                    (String) map.get(HTTPConstants.HEADER_SOAP_ACTION));
                Utils.configureMessageContextForHTTP((String)map.get(HTTPConstants.HEADER_CONTENT_TYPE),msgContext.getWSAAction(),msgContext);

                String requestURI = (String) map.get(HTTPConstants.REQUEST_URI);
                msgContext.setTo(
                    new EndpointReference(
                        AddressingConstants.WSA_TO,
                        requestURI));
                //getServiceLookUp(requestURI)));

                // TODO see is it a Service request e.g. WSDL, list ....
                // TODO take care of the other HTTPHeaders
            } else {
                if (HTTPConstants
                    .RESPONSE_ACK_CODE_VAL
                    .equals(map.get(HTTPConstants.RESPONSE_CODE))) {
                    msgContext.setProperty(
                        MessageContext.TRANSPORT_SUCCEED,
                        HTTPConstants.RESPONSE_ACK_CODE_VAL);
                    return soapEnvelope;
                }

                // TODO take care of other HTTP Headers
            }

            try {
                //Check for the REST behaviour, if you desire rest beahaviour
                //put a <parameter name="doREST" value="true"/> at the server.xml/client.xml file
                Object doREST = msgContext.getProperty(Constants.Configuration.DO_REST);
                XMLStreamReader xmlreader =
                    XMLInputFactory.newInstance().createXMLStreamReader(in);
                StAXBuilder builder = null;
                SOAPEnvelope envelope = null;
                if (doREST != null
                    && "true".equals(doREST)) {
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
        } else {
            throw new AxisFault("Input reader not found");
        }
    }

    /**
     * parses following two styles of HTTP stuff
     * Server Side
     * POST /axis2/services/echo HTTP/1.0
     * Content-Type: text/xml; charset=utf-8
     * Accept: application/soap+xml, application/dime, multipart/related, text
     * User-Agent: Axis/1.2RC1
     * Host: 127.0.0.1:8081
     * Cache-Control: no-cache
     * Pragma: no-cache
     * SOAPAction: ""
     * Content-Length: 73507
     * HTTP/1.1 200 OK
     * Content-Type: text/xml;charset=utf-8
     * Date: Sat, 12 Feb 2005 10:39:39 GMT
     * Server: Apache-Coyote/1.1
     * Connection: close
     *
     * @param reader
     * @param serverSide
     * @return
     * @throws AxisFault
     */
    public HashMap parseTheHeaders(Reader reader, boolean serverSide)
        throws AxisFault {
        HashMap map = new HashMap();
        try {
            StringBuffer str = new StringBuffer();
            int state = BEFORE_SEPERATOR;
            String key = null;
            String value = null;
            int start = 0;
            length = readLine(reader, buf);
            if (serverSide) {
                if ((buf[0] == 'P')
                    && (buf[1] == 'O')
                    && (buf[2] == 'S')
                    && (buf[3] == 'T')) {
                    index = 5;
                    value = readFirstLineArg(' ');
                    map.put(HTTPConstants.REQUEST_URI, value);
                    value = readFirstLineArg('\n');
                    map.put(HTTPConstants.PROTOCOL_VERSION, value);
                } else {
                    throw new AxisFault("Only the POST requests are supported");
                }
            } else {
                index = 0;
                value = readFirstLineArg(' ');
                map.put(HTTPConstants.PROTOCOL_VERSION, value);
                value = readFirstLineArg(' ');
                map.put(HTTPConstants.RESPONSE_CODE, value);
                value = readFirstLineArg('\n');
                map.put(HTTPConstants.RESPONSE_WORD, value);
            }
            state = BEFORE_SEPERATOR;
            while (!done) {
                length = readLine(reader, buf);
                if (length <= 0) {
                    throw new AxisFault("Premature end of steam");
                }
                for (int i = 0; i < length; i++) {
                    switch (state) {
                        case BEFORE_SEPERATOR :
                            if (buf[i] == ':') {
                                key = str.toString();
                                str = new StringBuffer();
                                state = AFTER_SEPERATOR;
                                if (buf[i + 1] == ' ') {
                                    i++; // ignore next space
                                }
                            } else {
                                str.append(buf[i]);
                            }
                            break;
                        case AFTER_SEPERATOR :
                            if (buf[i] == '\n') {
                                value = str.toString();
                                map.put(key, value);
                                str = new StringBuffer();
                                i = length;
                            } else {
                                str.append(buf[i]);
                            }
                            break;

                            // case END_OF_LINE :
                            // if (buf[i] == '\n') {
                            // state = END;
                            // break;
                            // } else {
                            // state = BEFORE_SEPERATOR;
                            // str.append(buf[i]);
                            // }
                            // break;
                            // case END:
                            // break;
                        default :
                            throw new AxisFault(
                                "Error Occured Unknown state " + state);
                    }
                }
                state = BEFORE_SEPERATOR;
            }
        } catch (IOException e) {
            throw new AxisFault(e.getMessage(), e);
        }
        return map;
    }

    // public HashMap parseTheHeaders(Reader reader, boolean serverSide)
    // throws AxisFault {
    // HashMap map = new HashMap();
    // try {
    // 
    // StringBuffer str = new StringBuffer();
    // 
    // int state = BEFORE_SEPERATOR;
    // 
    // String key = null;
    // String value = null;
    // 
    // int start = 0;
    // 
    // length = readLine(reader,buf);
    // 
    // 
    // 
    // if (serverSide) {
    // if (buf[0] == 'P'
    // && buf[1] == 'O'
    // && buf[2] == 'S'
    // && buf[3] == 'T') {
    // index = 5;
    // value = readFirstLineArg(' ');
    // map.put(HTTPConstants.REQUEST_URI,value );
    // value = readFirstLineArg('\n');
    // map.put(
    // HTTPConstants.PROTOCOL_VERSION,value);
    // start = index;
    // } else {
    // throw new AxisFault("Only the POST requests are supported");
    // }
    // } else {
    // index = 0;
    // value = readFirstLineArg(' ');
    // map.put(HTTPConstants.PROTOCOL_VERSION, value);
    // value = readFirstLineArg(' ');
    // map.put(HTTPConstants.RESPONSE_CODE,value);
    // value = readFirstLineArg('\n');
    // map.put(HTTPConstants.RESPONSE_WORD, value);
    // start = index;
    // }
    // 
    // while (state != END) {
    // if(length <= 0){
    // throw new AxisFault("Premature end of steam");
    // }
    // for (int i = start; i < length; i++) {
    // System.out.println(state);
    // switch (state) {
    // case BEFORE_SEPERATOR :
    // if (buf[i] == ':') {
    // key = str.toString();
    // str = new StringBuffer();
    // state = AFTER_SEPERATOR;
    // 
    // if(buf[i+1] == ' '){
    // i++;//ignore next space
    // }
    // } else {
    // str.append(buf[i]);
    // }
    // break;
    // case AFTER_SEPERATOR :
    // if (buf[i] == '\n') {
    // value = str.toString();
    // map.put(key, value);
    // str = new StringBuffer();
    // state = END_OF_LINE;
    // } else {
    // str.append(buf[i]);
    // }
    // break;
    // case END_OF_LINE :
    // if (buf[i] == '\n') {
    // state = END;
    // break;
    // } else {
    // state = BEFORE_SEPERATOR;
    // str.append(buf[i]);
    // }
    // break;
    // case END:
    // break;
    // default :
    // throw new AxisFault(
    // "Error Occured Unknown state " + state);
    // 
    // }
    // }
    // start = 0;
    // if(state != END){
    // length = reader.read(buf);
    // }
    // 
    // 
    // }
    // } catch (IOException e) {
    // throw new AxisFault(e.getMessage(), e);
    // }
    // return map;
    // }

    /**
     * Method readFirstLineArg
     *
     * @param terminal
     * @return
     * @throws AxisFault
     */
    private String readFirstLineArg(char terminal) throws AxisFault {
        StringBuffer str = new StringBuffer();
        try {
            while ((buf[index] != terminal) && (index < length)) {
                str.append(buf[index]);
                index++;
            }
            index++;
            return str.toString();
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
         * Read a single line from the input stream
         *
         * @param is  inputstream to read from
         * @param b   byte array to read into
         * @param off starting offset into the byte array
         * @param len maximum number of bytes to read
         * @return
         * @throws java.io.IOException
         */
    protected int readLine(Reader is, char[] b) throws java.io.IOException {
        int count = 0, c;

        // System.out.println("inside here");
        if (lastRead == -1) {
            c = is.read();
        } else {
            c = lastRead;
        }
        int off = 0;
        while (c != -1) {
            if ((c != '\n') && (c != '\r')) {
                b[off++] = (char) c;
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
            throw new AxisFault("Every line should ends with the \n, unexpected End of stream");
        } else {
            return (count > 0) ? count : -1;
        }
    }

    //    private String getServiceLookUp(String requestURI) throws AxisFault {
    //        final String URI_ID_STRING = "/services";
    //              String filePart = requestURI;
    //
    //              int index = filePart.lastIndexOf(URI_ID_STRING);
    //              String serviceStr = null;
    //              if (index > 0) {
    //                  serviceStr = filePart.substring(index + URI_ID_STRING.length() + 1);
    //                  return serviceStr;
    //
    //              } else {
    //                  throw new AxisFault("Both the URI and SOAP_ACTION are Null");
    //              }
    //    }
}
