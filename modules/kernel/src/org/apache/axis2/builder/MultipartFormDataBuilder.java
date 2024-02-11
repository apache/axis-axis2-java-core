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

package org.apache.axis2.builder;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.util.MultipleEntryHashMap;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.ParameterParser;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletRequestContext;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletDiskFileUpload;
import org.apache.commons.io.Charsets;

public class MultipartFormDataBuilder implements Builder {

    /**
     * @return Returns the document element.
     */
    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext)
            throws AxisFault {
        MultipleEntryHashMap parameterMap;
        HttpServletRequest request = null;
	try {
            request = (HttpServletRequest) messageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
        if (request == null) {
            throw new AxisFault("Cannot create DocumentElement without HttpServletRequest");
        }

        // TODO: Do check ContentLength for the max size,
        //       but it can't be configured anywhere.
        //       I think that it cant be configured at web.xml or axis2.xml.

        String charSetEncoding = (String)messageContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
        if (charSetEncoding == null) {
            charSetEncoding = request.getCharacterEncoding();
        }

        try {
            parameterMap = getParameterMap(request, charSetEncoding);
            return BuilderUtil.buildsoapMessage(messageContext, parameterMap,
                                                OMAbstractFactory.getSOAP12Factory());

        } catch (FileUploadException e) {
            throw AxisFault.makeFault(e);
        }

    }

    private MultipleEntryHashMap getParameterMap(HttpServletRequest request,
                                                 String charSetEncoding)
            throws FileUploadException {

        MultipleEntryHashMap parameterMap = new MultipleEntryHashMap();

        List items = parseRequest(new JakartaServletRequestContext(request));
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            DiskFileItem diskFileItem = (DiskFileItem)iter.next();

            boolean isFormField = diskFileItem.isFormField();

            Object value;
            try {
                if (isFormField) {
                    value = getTextParameter(diskFileItem, charSetEncoding);
                } else {
                    value = getFileParameter(diskFileItem);
                }
            } catch (Exception ex) {
                throw new FileUploadException(ex.getMessage());
            }
            parameterMap.put(diskFileItem.getFieldName(), value);
        }

        return parameterMap;
    }

    private static List parseRequest(JakartaServletRequestContext requestContext)
            throws FileUploadException {
        // Create a factory for disk-based file items
	DiskFileItemFactory fileItemFactory = DiskFileItemFactory.builder()
                .setCharset(StandardCharsets.UTF_8)
                .get();
        JakartaServletFileUpload upload = new JakartaServletFileUpload<>(fileItemFactory);
        // There must be a limit. 
        // This is for contentType="multipart/form-data"
        upload.setFileCountMax(1L);
        // Parse the request
        return upload.parseRequest(requestContext);
    }

    private String getTextParameter(DiskFileItem diskFileItem,
                                    String characterEncoding) throws Exception {

        String encoding = null;
	final ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        final Map<String, String> params = parser.parse(diskFileItem.getContentType(), ';');
        encoding = params.get("charset");
        if (encoding == null) {
            encoding = characterEncoding;
        }
        String textValue;
        if (encoding == null) {
            textValue = new String(diskFileItem.get());
        } else {
            textValue = new String(diskFileItem.get(), Charsets.toCharset(diskFileItem.getCharset(), StandardCharsets.ISO_8859_1));
        }

        return textValue;
    }

    private DataHandler getFileParameter(DiskFileItem diskFileItem)
            throws Exception {

        DataSource dataSource = new DiskFileDataSource(diskFileItem);
        DataHandler dataHandler = new DataHandler(dataSource);

        return dataHandler;
    }

}
