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
package org.apache.axis2.jaxws.context.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ContextUtils {
    private static final Log log = LogFactory.getLog(ContextUtils.class);
    private static final String WEBSERVICE_MESSAGE_CONTEXT = "javax.xml.ws.WebServiceContext";
    private static final OMElement[] ZERO_LENGTH_ARRAY = new OMElement[0];

    /**
     * Adds the appropriate properties to the MessageContext that the user will see
     *
     * @param soapMessageContext
     * @param jaxwsMessageContext
     */
    public static void addProperties(SOAPMessageContext soapMessageContext,
                                     MessageContext jaxwsMessageContext) {

        // Copy Axis2 MessageContext properties.  It's possible that some set of Axis2 handlers
        // have run and placed some properties in the context that need to be visible.
        soapMessageContext.putAll(jaxwsMessageContext.getProperties());

        EndpointDescription description = jaxwsMessageContext.getEndpointDescription();
        if (description !=null) {
            // Set the WSDL properties
            ServiceDescription sd =
                    description.getServiceDescription();
            if (sd != null) {
                URL wsdlLocation = ((ServiceDescriptionWSDL)sd).getWSDLLocation();
                if (wsdlLocation != null && !"".equals(wsdlLocation)) {
                    URI wsdlLocationURI = null;
                    try {
                        wsdlLocationURI = wsdlLocation.toURI();
                    }
                    catch (URISyntaxException ex) {
                        // TODO: NLS/RAS
                        log.warn("Unable to convert WSDL location URL to URI.  URL: " +
                                wsdlLocation.toString() + "; Service: " + description.getServiceQName(), ex);
                    }
                    soapMessageContext
                            .put(javax.xml.ws.handler.MessageContext.WSDL_DESCRIPTION, wsdlLocationURI);
                    soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.WSDL_DESCRIPTION,
                                                Scope.APPLICATION);
                }
    
                soapMessageContext
                        .put(javax.xml.ws.handler.MessageContext.WSDL_SERVICE, description.getServiceQName());
                soapMessageContext
                        .setScope(javax.xml.ws.handler.MessageContext.WSDL_SERVICE, Scope.APPLICATION);
                if (log.isDebugEnabled()) {
                    log.debug("WSDL_SERVICE :" + description.getServiceQName());
                }
            }
        }

        //Retrieve any reference parameters from the axis2 message context, and set
        //them on the soap message context.
        org.apache.axis2.context.MessageContext msgContext =
            jaxwsMessageContext.getAxisMessageContext();
        EndpointReference epr = msgContext.getTo();
        Map<String, OMElement> referenceParameters = epr.getAllReferenceParameters();
        
        if (referenceParameters != null) {
        	OMElement[] omElements = referenceParameters.values().toArray(ZERO_LENGTH_ARRAY);
        	List<Element> list = new ArrayList<Element>();
        	
        	try {
        		for (OMElement omElement : omElements) {
        			Element element = XMLUtils.toDOM(omElement);
        			list.add(element);
        		}
        	}
        	catch (Exception e) {
                //TODO NLS enable.
                throw ExceptionFactory.makeWebServiceException("Error during processing of reference parameters.", e);
        	}
        	            
            soapMessageContext
                    .put(javax.xml.ws.handler.MessageContext.REFERENCE_PARAMETERS, list);
            soapMessageContext
                    .setScope(javax.xml.ws.handler.MessageContext.REFERENCE_PARAMETERS, Scope.APPLICATION);

            if (log.isDebugEnabled()) {
                log.debug("Reference Parameters set.");
            }
        }
        else {
        	if (log.isDebugEnabled()) {
      			log.debug("No Reference Parameters found.");
        	}
        }
        
        // If we are running within a servlet container, then JAX-WS requires that the
        // servlet related properties be set on the MessageContext
        soapMessageContext.put(javax.xml.ws.handler.MessageContext.SERVLET_CONTEXT,
                               jaxwsMessageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT));
        soapMessageContext
                .setScope(javax.xml.ws.handler.MessageContext.SERVLET_CONTEXT, Scope.APPLICATION);

        if (log.isDebugEnabled()) {
            if (jaxwsMessageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT) != null) {
                log.debug("Servlet Context Set");
            } else {
                log.debug("Servlet Context not found");
            }
        }

        HttpServletRequest req = (HttpServletRequest)jaxwsMessageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        if (req == null) {
            if (log.isDebugEnabled()) {
                log.debug("HTTPServletRequest not found");
            }
        }
        if (req != null) {
            soapMessageContext.put(javax.xml.ws.handler.MessageContext.SERVLET_REQUEST, req);
            soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.SERVLET_REQUEST,
                                        Scope.APPLICATION);
            if (log.isDebugEnabled()) {
                log.debug("SERVLET_REQUEST Set");
            }

            String pathInfo = req.getPathInfo();
            soapMessageContext.put(javax.xml.ws.handler.MessageContext.PATH_INFO, pathInfo);
            soapMessageContext
                    .setScope(javax.xml.ws.handler.MessageContext.PATH_INFO, Scope.APPLICATION);
            if (log.isDebugEnabled()) {
                if (pathInfo != null) {
                    log.debug("HTTP_REQUEST_PATHINFO Set");
                } else {
                    log.debug("HTTP_REQUEST_PATHINFO not found");
                }
            }
            String queryString = req.getQueryString();
            soapMessageContext.put(javax.xml.ws.handler.MessageContext.QUERY_STRING, queryString);
            soapMessageContext
                    .setScope(javax.xml.ws.handler.MessageContext.QUERY_STRING, Scope.APPLICATION);
            if (log.isDebugEnabled()) {
                if (queryString != null) {
                    log.debug("HTTP_REQUEST_QUERYSTRING Set");
                } else {
                    log.debug("HTTP_REQUEST_QUERYSTRING not found");
                }
            }
            String method = req.getMethod();
            soapMessageContext.put(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_METHOD, method);
            soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.HTTP_REQUEST_METHOD,
                                        Scope.APPLICATION);
            if (log.isDebugEnabled()) {
                if (method != null) {
                    log.debug("HTTP_REQUEST_METHOD Set");
                } else {
                    log.debug("HTTP_REQUEST_METHOD not found");
                }
            }

        }
        HttpServletResponse res = (HttpServletResponse)jaxwsMessageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
        if (res == null) {
            if (log.isDebugEnabled()) {
                log.debug("Servlet Response not found");
            }
        }
        if (res != null) {
            soapMessageContext.put(javax.xml.ws.handler.MessageContext.SERVLET_RESPONSE, res);
            soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.SERVLET_RESPONSE,
                                        Scope.APPLICATION);
            if (log.isDebugEnabled()) {
                log.debug("SERVLET_RESPONSE Set");
            }
        }
        
    }

    public static void addWSDLProperties(MessageContext jaxwsMessageContext) {
        org.apache.axis2.context.MessageContext msgContext =
                jaxwsMessageContext.getAxisMessageContext();
        ServiceContext serviceContext = msgContext.getServiceContext();
        SOAPMessageContext soapMessageContext = null;
        if (serviceContext != null) {
            WebServiceContext wsc =
                    (WebServiceContext)serviceContext.getProperty(WEBSERVICE_MESSAGE_CONTEXT);
            if (wsc != null) {
                soapMessageContext = (SOAPMessageContext)wsc.getMessageContext();
            }
        }
        OperationDescription op = jaxwsMessageContext.getOperationDescription();

        if (op != null && soapMessageContext != null) {
            soapMessageContext
                    .put(javax.xml.ws.handler.MessageContext.WSDL_OPERATION, op.getName());
            soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.WSDL_OPERATION,
                                        Scope.APPLICATION);
            if (log.isDebugEnabled()) {
                log.debug("WSDL_OPERATION :" + op.getName());
            }

            EndpointInterfaceDescription eid = op.getEndpointInterfaceDescription();
            if (eid != null) {
                EndpointDescription ed = eid.getEndpointDescription();
                QName portType = eid.getPortType();
                if (portType == null || portType.getLocalPart() == "") {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "Did not get port type from EndpointInterfaceDescription, attempting to get PortType from EndpointDescription");
                    }
                }
                if (ed != null) {
                    soapMessageContext
                            .put(javax.xml.ws.handler.MessageContext.WSDL_PORT, ed.getPortQName());
                    soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.WSDL_PORT,
                                                Scope.APPLICATION);
                    if (log.isDebugEnabled()) {
                        log.debug("WSDL_PORT :" + ed.getPortQName());
                    }
                }
                soapMessageContext
                        .put(javax.xml.ws.handler.MessageContext.WSDL_INTERFACE, portType);
                soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.WSDL_INTERFACE,
                                            Scope.APPLICATION);
                if (log.isDebugEnabled()) {
                    log.debug("WSDL_INTERFACE :" + portType);
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unable to read WSDL operation, port and interface properties");
            }
        }
    }
    
    public static void addWSDLProperties_provider(MessageContext jaxwsMessageContext) {
        org.apache.axis2.context.MessageContext msgContext =
                jaxwsMessageContext.getAxisMessageContext();
        ServiceContext serviceContext = msgContext.getServiceContext();
        SOAPMessageContext soapMessageContext = null;
        if (serviceContext != null) {
            WebServiceContext wsc =
                    (WebServiceContext)serviceContext.getProperty(WEBSERVICE_MESSAGE_CONTEXT);
            if (wsc != null) {
                soapMessageContext = (SOAPMessageContext)wsc.getMessageContext();
            }
        }

        String op = jaxwsMessageContext.getOperationName().getLocalPart();    	
        
        if (op != null && soapMessageContext != null) {
            soapMessageContext
                    .put(javax.xml.ws.handler.MessageContext.WSDL_OPERATION, op);
            soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.WSDL_OPERATION,
                                        Scope.APPLICATION);
            if (log.isDebugEnabled()) {
                log.debug("WSDL_OPERATION :" + op);
            }

            //EndpointInterfaceDescription eid = op.getEndpointInterfaceDescription();
            EndpointDescription ed = jaxwsMessageContext.getEndpointDescription();
            
            if (ed != null) {
                    soapMessageContext
                            .put(javax.xml.ws.handler.MessageContext.WSDL_PORT, ed.getPortQName());
                    soapMessageContext.setScope(javax.xml.ws.handler.MessageContext.WSDL_PORT,
                                                Scope.APPLICATION);
                    if (log.isDebugEnabled()) {
                        log.debug("WSDL_PORT :" + ed.getPortQName());
                    }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unable to read WSDL operation, port and interface properties");
            }
        }
    }


}
