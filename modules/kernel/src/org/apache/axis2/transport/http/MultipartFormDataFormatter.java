/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.transport.http;

import org.apache.axiom.mime.Header;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.OMMultipartWriter;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.util.URLTemplatingUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;

/**
 * Formates the request message as multipart/form-data. An example of this serialization is shown
 * below which was extracted from the Web Services Description Language (WSDL) Version 2.0 Part 2: Adjuncts
 * <p/>
 * The following instance data of an input message:
 * <p/>
 * <data>
 * <town>
 * <name>Fréjus</name>
 * <country>France</country>
 * </town>
 * <date>@@@@-@@-@@</date>
 * </data>
 * <p/>
 * with the following operation element
 * <p/>
 * <operation ref='t:data'
 * whttp:location='temperature'
 * whttp:method='POST'
 * whttp:inputSerialization='multipart/form-data'/>
 * <p/>
 * will serialize the message as follow:
 * <p/>
 * Content-Type: multipart/form-data; boundary=AaB03x
 * Content-Length: xxx
 * <p/>
 * --AaB03x
 * Content-Disposition: form-data; name="town"
 * Content-Type: application/xml
 * <p/>
 * <town>
 * <name>Fréjus</name>
 * <country>France</country>
 * </town>
 * --AaB03x
 * Content-Disposition: form-data; name="date"
 * Content-Type: text/plain; charset=utf-8
 *
 * @@@@-@@-@@ --AaB03x--
 */
public class MultipartFormDataFormatter implements MessageFormatter {
    /**
     * Different message formats can set their own content types
     * Eg: JSONFormatter can set the content type as application/json
     *
     * @param messageContext
     * @param format
     * @param soapAction
     */
    public String getContentType(MessageContext messageContext, OMOutputFormat format,
                                 String soapAction) {

        String contentType = HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA;
        String encoding = format.getCharSetEncoding();
        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }
        contentType = contentType + "; " + "boundary=" + format.getMimeBoundary();
        return contentType;
    }

    /**
     * Some message formats may want to alter the target url.
     *
     * @return the target URL
     */
    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat format, URL targetURL)
            throws AxisFault {
        // Check whether there is a template in the URL, if so we have to replace then with data
        // values and create a new target URL.
        targetURL = URLTemplatingUtil.getTemplatedURL(targetURL, messageContext, false);

        return targetURL;
    }

    /**
     * @return this only if you want set a transport header for SOAP Action
     */
    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat format,
                                   String soapAction) {
        return soapAction;
    }

    public void writeTo(MessageContext messageContext, OMOutputFormat format,
            OutputStream outputStream, boolean preserve) throws AxisFault {
        OMElement dataOut = messageContext.getEnvelope().getBody().getFirstElement();
        if (dataOut != null) {
            try {
                Iterator iter1 = dataOut.getChildElements();
                OMFactory omFactory = OMAbstractFactory.getOMFactory();
                OMMultipartWriter writer = new OMMultipartWriter(outputStream, format);
                while (iter1.hasNext()) {
                    OMElement ele = (OMElement) iter1.next();
                    Iterator iter2 = ele.getChildElements();
                    // check whether the element is a complex type
                    if (iter2.hasNext()) {
                        OMElement omElement =
                                omFactory.createOMElement(ele.getQName().getLocalPart(), null);
                        omElement.addChild(
                                processComplexType(omElement, ele.getChildElements(), omFactory));
                        OutputStream partOutputStream = writer.writePart(DEFAULT_CONTENT_TYPE, null,
                                Collections.singletonList(new Header("Content-Disposition",
                                        DISPOSITION_TYPE + "; name=\"" + omElement.getLocalName() + "\"")));
                        partOutputStream.write(omElement.toString().getBytes());
                        partOutputStream.close();
                    } else {
                        OutputStream partOutputStream = writer.writePart(DEFAULT_CONTENT_TYPE, null,
                                Collections.singletonList(new Header("Content-Disposition",
                                        DISPOSITION_TYPE + "; name=\"" + ele.getLocalName() + "\"")));
                        partOutputStream.write(ele.getText().getBytes());
                        partOutputStream.close();
                    }
                }
                writer.complete();
            } catch (IOException ex) {
                throw AxisFault.makeFault(ex);
            }
        }
    }

    /**
     * @param parent
     * @param iter
     * @param omFactory
     * @return
     */
    private OMElement processComplexType(OMElement parent, Iterator iter, OMFactory omFactory) {

        OMElement omElement = null;
        while (iter.hasNext()) {
            OMElement ele = (OMElement) iter.next();
            omElement = omFactory.createOMElement(ele.getQName().getLocalPart(), null);
            Iterator iter2 = ele.getChildElements();
            if (iter2.hasNext()) {
                parent.addChild(processComplexType(omElement, ele.getChildElements(), omFactory));
            } else {
                omElement = omFactory.createOMElement(ele.getQName().getLocalPart(), null);
                omElement.setText(ele.getText());
                parent.addChild(omElement);
            }
        }
        return omElement;
    }
    
    public static final String DEFAULT_CONTENT_TYPE = "text/plain; charset=US-ASCII";
    public static final String DISPOSITION_TYPE = "form-data";
    
}
