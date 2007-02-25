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


package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Dispatches the operation based on the information from the target endpoint URL.
 */
public class HTTPLocationBasedDispatcher extends AbstractDispatcher {

    public static final String NAME = "HTTPLocationBasedDispatcher";
    private static final Log log = LogFactory.getLog(HTTPLocationBasedDispatcher.class);

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findOperation(org.apache.axis2.description.AxisService, org.apache.axis2.context.MessageContext)
     */
    public AxisOperation findOperation(AxisService service, MessageContext messageContext)
            throws AxisFault {

        AxisService axisService = messageContext.getAxisService();
        if (axisService != null) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            if (httpServletRequest != null) {
                String uri = httpServletRequest.getPathInfo();
                String query;
                if ((query = httpServletRequest.getQueryString()) != null) {
                    uri = uri + Constants.METHOD_NAME_ESCAPE_CHARACTER + query;
                }

                String httpLocation = parseRequestURL(uri);

                if (httpLocation != null) {
                    AxisEndpoint axisEndpoint = (AxisEndpoint) messageContext
                            .getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME);
                    if (axisEndpoint != null) {
                        Map httpLocationTable = (Map) axisEndpoint.getBinding()
                                .getProperty(WSDL2Constants.HTTP_LOCATION_TABLE);
                        if (httpLocationTable != null) {
                            AxisOperation axisOperation =
                                    getOperationFromHTTPLocation(httpLocation, httpLocationTable);
                            return axisOperation;
                        }
                    }
                } else {
                    log.debug("Attempt to check for Operation using HTTP Location failed");
                    return null;
                }
            } else {
                log.debug("HttpServletRequest in MessageContext is null");
                return null;
            }
        }
        return null;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        // This Dispatcher does not need to resolve the service, as that is handled
        // by the RequestURIBasedDispatcher.
        return null;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }

    private String parseRequestURL (String path) {

            path = path.substring(1);
            int index = path.indexOf("/");
            String service = null;

            if (-1 != index) {
                service =  path.substring(index);
            }
             else {
                int queryIndex = path.indexOf("?");
                if (queryIndex != -1) {
                    service = path.substring(queryIndex);
                }
            }
             return service;
   }

    /**
     * Given the requestPath that the request came to his method returns the corresponding axisOperation
     *
     * @param requestPath - Part of the request url which is the part after the service name
     * @return AxisOperation - The corresponding AxisOperation
     */
    private AxisOperation getOperationFromHTTPLocation(String requestPath, Map httpLocationTable) {

        Collection httpLocations = httpLocationTable.keySet();
        Iterator iter = httpLocations.iterator();
        while (iter.hasNext()) {
            String location = (String) iter.next();
            int index = requestPath.indexOf(location);
            if (index == 0) {
                return (AxisOperation) httpLocationTable.get(location);
            }

        }
        return null;
    }
}