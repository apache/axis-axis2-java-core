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
package org.apache.axis2.transport.http.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @deprecated Since we are not using this class and this might lead to mis-use of this class, we will
 * removing this class in a future release.
 */
public class SOAPUtil {
    private static final Log log = LogFactory.getLog(SOAPUtil.class);

    public SOAPUtil() {
    }

    /**
     * Handle SOAP Messages
     *
     * @param msgContext
     * @param request
     * @param response
     * @throws AxisFault
     */
    public boolean processPostRequest(MessageContext msgContext,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws AxisFault {
        try {
            response.setHeader("Content-Type","text/html");

            if(server(msgContext) != null){
                response.setHeader("Server",server(msgContext));
            }
            String soapAction = request.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
            HTTPTransportUtils.processHTTPPostRequest(msgContext,
                                                      request.getInputStream(),
                                                      response.getOutputStream(),
                                                      request.getContentType(),
                                                      soapAction,
                                                      request.getRequestURL().toString());

            Object contextWritten = null;
            if (msgContext.getOperationContext() != null) {
                contextWritten = msgContext.getOperationContext().getProperty(Constants.RESPONSE_WRITTEN);
            }

            response.setContentType("text/xml; charset="
                                    + msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING));

            if ((contextWritten == null) || !Constants.VALUE_TRUE.equals(contextWritten)) {
                Integer statusCode = (Integer) msgContext.getProperty(Constants.RESPONSE_CODE);
                if (statusCode != null) {
                    response.setStatus(statusCode.intValue());
                } else {
                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            }

            boolean closeReader = true;
            Parameter parameter = msgContext.getConfigurationContext().getAxisConfiguration().getParameter("axis2.close.reader");
            if (parameter != null) {
                closeReader = JavaUtils.isTrueExplicitly(parameter.getValue());
            }
            if (closeReader) {
                try {
                    ((StAXBuilder) msgContext.getEnvelope().getBuilder()).close();
                } catch (Exception e) {
                    log.debug(e);
                }
            }
            return true;
        } catch (AxisFault axisFault) {
            throw axisFault;
        }
        catch (IOException ioException) {
            throw new AxisFault(ioException);
        }
    }

    private String server(MessageContext messageContext) {
        if (messageContext.getParameter(HTTPConstants.SERVER) != null){
            OMElement userAgentElement = messageContext.getParameter(HTTPConstants.SERVER).getParameterElement();
            return userAgentElement.getText().trim();

        }
        return null;

    }
}
