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

package org.apache.axis2.builder;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MultipleEntryHashMap;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class MultipartFormDataBuilder implements Builder {

    /**
     * @return Returns the document element.
     */
    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext)
            throws AxisFault {
        MultipleEntryHashMap parameterMap;
        HttpServletRequest request = (HttpServletRequest) messageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        if (request == null) {
            throw new AxisFault("Cannot create DocumentElement without HttpServletRequest");
        }
        try {
            parameterMap = getParameterMap(request);
            return BuilderUtil.buildsoapMessage(messageContext, parameterMap,
                                                OMAbstractFactory.getSOAP12Factory());

        } catch (FileUploadException e) {
            throw AxisFault.makeFault(e);
        }

    }


    private MultipleEntryHashMap getParameterMap(HttpServletRequest request)
            throws FileUploadException {

        MultipleEntryHashMap parameterMap = new MultipleEntryHashMap();

        List items = parseRequest(new ServletRequestContext(request));
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            String[] value = new String[1];
            DiskFileItem diskFileItem = (DiskFileItem) iter.next();
            value[0] = diskFileItem.getString();
            parameterMap.put(diskFileItem.getFieldName(), value);
        }

        return parameterMap;
    }

    private static List parseRequest(ServletRequestContext requestContext)
            throws FileUploadException {
        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        // Parse the request
        return upload.parseRequest(requestContext);
    }

}
