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

package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.i18n.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Class HTTPTransportReceiver
 */
public class HTTPTransportReceiver {
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
    private byte[] buf = new byte[1024];

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
     * @param in
     * @param serverSide
     * @return
     * @throws AxisFault
     */
    public HashMap parseTheHeaders(InputStream in, boolean serverSide)
            throws AxisFault {
        HashMap map = new HashMap();
        try {
            StringBuffer str = new StringBuffer();
            int state = BEFORE_SEPERATOR;
            String key = null;
            String value = null;
            length = readLine(in, buf);
            if (serverSide) {
                if ((buf[0] == 'P')
                        && (buf[1] == 'O')
                        && (buf[2] == 'S')
                        && (buf[3] == 'T')) {
                    map.put(HTTPConstants.HTTP_REQ_TYPE,
                            HTTPConstants.HEADER_POST);
                    index = 5;

                } else if (
                        (buf[0] == 'G') && (buf[1] == 'E') && (buf[2] == 'T')) {
                    map.put(HTTPConstants.HTTP_REQ_TYPE,
                            HTTPConstants.HEADER_GET);
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
                if (value != null && value.indexOf("HTTP") >= 0) {
                    map.put(HTTPConstants.PROTOCOL_VERSION, value);
                    value = readFirstLineArg(' ');
                    map.put(HTTPConstants.RESPONSE_CODE, value);
                } else {
                    map.put(HTTPConstants.RESPONSE_CODE, value);
                }

                value = readFirstLineArg('\n');
                map.put(HTTPConstants.RESPONSE_WORD, value);
            }
            state = BEFORE_SEPERATOR;
            while (!done) {
                length = readLine(in, buf);
                if (length <= 0) {
                    throw new AxisFault(Messages.getMessage("preatureEOS"));
                }
                for (int i = 0; i < length; i++) {
                    switch (state) {
                        case BEFORE_SEPERATOR:
                            if (buf[i] == ':') {
                                key = str.toString();
                                str = new StringBuffer();
                                state = AFTER_SEPERATOR;
                                if (buf[i + 1] == ' ') {
                                    i++; // ignore next space
                                }
                            } else {
                                str.append((char) buf[i]);
                            }
                            break;
                        case AFTER_SEPERATOR:
                            if (buf[i] == '\n') {
                                value = str.toString();
                                map.put(key, value);
                                str = new StringBuffer();
                                i = length;
                            } else {
                                str.append((char) buf[i]);
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


    /**
     * Method readFirstLineArg
     *
     * @param terminal
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    private String readFirstLineArg(char terminal) throws AxisFault {
        StringBuffer str = new StringBuffer();
        try {
            while ((buf[index] != terminal) && (index < length)) {
                str.append((char) buf[index]);
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
     * @return
     * @throws java.io.IOException
     */
    protected int readLine(InputStream is, byte[] b)
            throws java.io.IOException {
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
                b[off++] = (byte) c;
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
            throw new AxisFault(
                    "Every line should ends with the \\n, unexpected End of stream");
        } else {
            return (count > 0) ? count : -1;
        }
    }


    /**
     * Returns the HTML text for the list of services deployed
     * This can be delegated to another Class as well
     * where it will handle more options of GET messages :-?
     *
     * @return
     */
    public static String getServicesHTML(
            ConfigurationContext configurationContext) {
        String temp = "";
        Map services =
                configurationContext.getAxisConfiguration().getServices();
        Hashtable erroneousServices =
                configurationContext.getAxisConfiguration().getFaultyServices();
        boolean status = false;

        if (services != null && !services.isEmpty()) {
            status = true;
            Collection serviceCollection = services.values();
            temp += "<h2>" + "Deployed services" + "</h2>";
            for (Iterator it = serviceCollection.iterator(); it.hasNext();) {
                Map operations;
                Collection operationsList;
                AxisService axisService = (AxisService) it.next();
                operations = axisService.getOperations();
                operationsList = operations.values();

                temp += "<h3>" + axisService.getName().getLocalPart() +
                        "</h3>";
                if (operationsList.size() > 0) {
                    temp += "Available operations <ul>";
                    for (Iterator iterator1 = operationsList.iterator();
                         iterator1.hasNext();
                            ) {
                        AxisOperation axisOperation =
                                (AxisOperation) iterator1.next();
                        temp += "<li>"
                                + axisOperation.getName().getLocalPart()
                                + "</li>";
                    }
                    temp += "</ul>";
                } else {
                    temp += "No operations speficied for this service";
                }
            }
        }

        if (erroneousServices != null && !erroneousServices.isEmpty()) {

            temp += "<hr><h2><font color=\"blue\">Faulty Services</font></h2>";
            status = true;
            Enumeration faultyservices = erroneousServices.keys();
            while (faultyservices.hasMoreElements()) {
                String faultyserviceName =
                        (String) faultyservices.nextElement();
                temp += "<h3><font color=\"blue\">"
                        + faultyserviceName
                        + "</font></h3>";
            }
        }

        if (!status) {
            temp = "<h2>There are no services deployed</h2>";
        }

        temp =
                "<html><head><title>Axis2: Services</title></head>"
                        + "<body>"
                        + temp
                        + "</body></html>";

        return temp;
    }

    public static Map getGetRequestParameters(String requestURI) {
        Map map = new HashMap();

        char[] chars = requestURI.toCharArray();
        final int NOT_BEGUN = 1500;
        final int INSIDE_NAME = 1501;
        final int INSIDE_VALUE = 1502;

        int state = NOT_BEGUN;
        StringBuffer name = new StringBuffer();
        StringBuffer value = new StringBuffer();

        for (int index = 0; index < chars.length; index++) {
            if (state == NOT_BEGUN) {
                if (chars[index] == '?') {
                    state = INSIDE_NAME;
                }
            } else if (state == INSIDE_NAME) {
                if (chars[index] == '=') {
                    state = INSIDE_VALUE;
                } else {
                    name.append(chars[index]);
                }
            } else if (state == INSIDE_VALUE) {
                if (chars[index] == ',') {
                    state = INSIDE_NAME;
                    map.put(name.toString(), value.toString());
                    name.delete(0, name.length());
                    value.delete(0, value.length());
                } else {
                    value.append(chars[index]);
                }
            }
        }
        if (name.length() + value.length() > 0) {
            map.put(name.toString(), value.toString());
        }
        return map;
    }

}
