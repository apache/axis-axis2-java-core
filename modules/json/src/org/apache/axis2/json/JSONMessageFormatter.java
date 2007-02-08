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

package org.apache.axis2.json;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.ArrayList;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;


public class JSONMessageFormatter implements MessageFormatter {

    public String getContentType(MessageContext msgCtxt, OMOutputFormat format, String soapActionString) {
        String contentType;
        String encoding = format.getCharSetEncoding();
        if (msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE) != null) {
            contentType = (String) msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
        } else {
            contentType = (String) msgCtxt.getProperty(Constants.Configuration.MESSAGE_TYPE);
        }

        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }
        return contentType;
    }

    public byte[] getBytes(MessageContext msgCtxt, OMOutputFormat format) throws AxisFault {
        OMElement element = msgCtxt.getEnvelope().getBody().getFirstElement();
        if (element instanceof OMSourcedElementImpl && getStringToWrite(((OMSourcedElementImpl) element).getDataSource()) != null)
        {
            String jsonToWrite = getStringToWrite(((OMSourcedElementImpl) element).getDataSource());
            return jsonToWrite.getBytes();
        } else {
            try {
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                XMLStreamWriter jsonWriter = getJSONWriter(bytesOut);
                element.serializeAndConsume(jsonWriter);
                jsonWriter.writeEndDocument();

                return bytesOut.toByteArray();

            } catch (XMLStreamException e) {
                throw new AxisFault(e);
            } catch (FactoryConfigurationError e) {
                throw new AxisFault(e);
            }
        }
    }

    public String formatSOAPAction(MessageContext msgCtxt, OMOutputFormat format, String soapActionString) {
        return null;
    }

    protected XMLStreamWriter getJSONWriter(OutputStream outStream) {
        MappedNamespaceConvention mnc = new MappedNamespaceConvention();
        return new MappedXMLStreamWriter(mnc, new OutputStreamWriter(outStream));
    }

    protected String getStringToWrite(OMDataSource dataSource) {
        if (dataSource instanceof JSONDataSource) {
            return ((JSONDataSource) dataSource).getCompleteJOSNString();
        } else {
            return null;
        }
    }

    public void writeTo(MessageContext msgCtxt, OMOutputFormat format,
                        OutputStream out, boolean preserve) throws AxisFault {
        OMElement element = msgCtxt.getEnvelope().getBody().getFirstElement();
        try {
            if (element instanceof OMSourcedElementImpl && getStringToWrite(((OMSourcedElementImpl) element).getDataSource()) != null)
            {
                String jsonToWrite = getStringToWrite(((OMSourcedElementImpl) element).getDataSource());
                out.write(jsonToWrite.getBytes());
            } else {
                XMLStreamWriter jsonWriter = getJSONWriter(out);
                element.serializeAndConsume(jsonWriter);
                jsonWriter.writeEndDocument();
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        }
    }

    public URL getTargetAddress(MessageContext msgCtxt, OMOutputFormat format, URL targetURL) throws AxisFault {

        String httpMethod =
                (String) msgCtxt.getProperty(Constants.Configuration.HTTP_METHOD);

        if ((httpMethod != null)
                && Constants.Configuration.HTTP_METHOD_GET.equalsIgnoreCase(httpMethod)) {
            String param = getParam(msgCtxt);

            if (param != null && param.length() > 0) {
                String returnURLFile = targetURL.getFile() + "?" + param;
                try {
                    return new URL(targetURL.getProtocol(), targetURL.getHost(), targetURL.getPort(), returnURLFile);
                } catch (MalformedURLException e) {
                    throw new AxisFault(e);
                }
            } else {
                return targetURL;
            }
        } else {
            return targetURL;
        }
    }

    private String getParam(MessageContext msgContext) {
        OMElement dataOut;

        dataOut = msgContext.getEnvelope().getBody().getFirstElement();

        Iterator iter1 = dataOut.getChildElements();
        ArrayList paraList = new ArrayList();

        while (iter1.hasNext()) {
            OMElement ele = (OMElement) iter1.next();
            String parameter;

            parameter = ele.getLocalName() + "=" + ele.getText();
            paraList.add(parameter);
        }

        String paraString = "";
        int count = paraList.size();

        for (int i = 0; i < count; i++) {
            String c = (String) paraList.get(i);
            paraString = "".equals(paraString) ? c : (paraString + "&" + c);
        }

        return paraString;
    }

}
