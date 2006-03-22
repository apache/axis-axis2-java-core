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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Class HTTPTransportReceiver
 */
public class HTTPTransportReceiver {

    /**
     * Field BEFORE_SEPARATOR
     */
    private static final int BEFORE_SEPARATOR = 3;

    /**
     * Field AFTER_SEPARATOR
     */
    private static final int AFTER_SEPARATOR = 4;

    /**
     * Field lastRead
     */
    private int lastRead = -1;

    /**
     * Field index
     */
    int index = 0;

    /**
     * Field buf
     */
    private byte[] buf = new byte[1024];

    /**
     * Field length
     */
    int length = 0;

    /**
     * Field done
     */
    private boolean done = false;

    /**
     * Parses following two styles of HTTP stuff
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
                                str.append((char) buf[i]);
                            }

                            break;

                        case AFTER_SEPARATOR :
                            if (buf[i] == '\n') {
                                value = str.toString();
                                map.put(key, value);
                                str = new StringBuffer();
                                i = length;
                            } else {
                                str.append((char) buf[i]);
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
            throw new AxisFault("Every line should ends with the \\n, unexpected End of stream");
        } else {
            return (count > 0)
                    ? count
                    : -1;
        }
    }

    public static Map getGetRequestParameters(String requestURI) {

        Map map = new HashMap();
        if (requestURI == null || "".equals(requestURI)) {
            return map;
        }
        char[]       chars = requestURI.toCharArray();
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

    /**
     * Returns the HTML text for the list of services deployed.
     * This can be delegated to another Class as well
     * where it will handle more options of GET messages.
     *
     * @return Returns String.
     */
    public static String getServicesHTML(ConfigurationContext configurationContext) {
        String temp = "";
        Map services = configurationContext.getAxisConfiguration().getServices();
        Hashtable erroneousServices =
                configurationContext.getAxisConfiguration().getFaultyServices();
        boolean status = false;

        if ((services != null) && !services.isEmpty()) {
            status = true;

            Collection serviceCollection = services.values();

            temp += "<h2>" + "Deployed services" + "</h2>";

            for (Iterator it = serviceCollection.iterator(); it.hasNext();) {

                AxisService axisService = (AxisService) it.next();

                Iterator iterator = axisService.getOperations();

                temp += "<h3><a href=\"" + axisService.getName() + "?wsdl\">" + axisService.getName() + "</a></h3>";

                if (iterator.hasNext()) {
                    temp += "Available operations <ul>";

                    for (; iterator.hasNext();) {
                        AxisOperation axisOperation = (AxisOperation) iterator.next();

                        temp += "<li>" + axisOperation.getName().getLocalPart() + "</li>";
                    }

                    temp += "</ul>";
                } else {
                    temp += "No operations specified for this service";
                }
            }
        }

        if ((erroneousServices != null) && !erroneousServices.isEmpty()) {
            temp += "<hr><h2><font color=\"blue\">Faulty Services</font></h2>";
            status = true;

            Enumeration faultyservices = erroneousServices.keys();

            while (faultyservices.hasMoreElements()) {
                String faultyserviceName = (String) faultyservices.nextElement();

                temp += "<h3><font color=\"blue\">" + faultyserviceName + "</font></h3>";
            }
        }

        if (!status) {
            temp = "<h2>There are no services deployed</h2>";
        }

        temp = "<html><head><title>Axis2: Services</title></head>" + "<body>" + temp
                + "</body></html>";

        return temp;
    }

    public static String printServiceHTML(String serviceName,
                                          ConfigurationContext configurationContext) {
        String temp = "";
        try {
            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
            AxisService axisService = axisConfig.getService(serviceName);
            Iterator iterator = axisService.getOperations();
            temp += "<h3>" + axisService.getName() + "</h3>";
            temp += "<a href=\"" + axisService.getName() + "?wsdl\">wsdl</a> <br/> ";
            temp += "<i>Service Description :  " + axisService.getServiceDescription() + "</i><br/><br/>";
            if (iterator.hasNext()) {
                temp += "Available operations <ul>";
                for (; iterator.hasNext();) {
                    AxisOperation axisOperation = (AxisOperation) iterator.next();
                    temp += "<li>" + axisOperation.getName().getLocalPart() + "</li>";
                }
                temp += "</ul>";
            } else {
                temp += "No operations specified for this service";
            }
            temp = "<html><head><title>Axis2: Services</title></head>" + "<body>" + temp
                    + "</body></html>";
        }
        catch (AxisFault axisFault) {
            temp = "<html><head><title>Service has a fualt</title></head>" + "<body>"
                    + "<hr><h2><font color=\"blue\">" + axisFault.getMessage() + "</font></h2></body></html>";
        }
        return temp;
    }
}
