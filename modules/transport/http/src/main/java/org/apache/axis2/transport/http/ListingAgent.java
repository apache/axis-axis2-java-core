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

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.transport.http.server.HttpUtils;
import org.apache.axis2.util.*;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyRegistry;
import org.apache.neethi.PolicyRegistryImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

public class ListingAgent extends AbstractAgent {

    private static final OnDemandLogger log = new OnDemandLogger(ListingAgent.class);

    private static final String LIST_MULTIPLE_SERVICE_JSP_NAME =
            "listServices.jsp";
    private static final String LIST_FAULTY_SERVICES_JSP_NAME = "listFaultyService.jsp";

    public ListingAgent(ConfigurationContext aConfigContext) {
        super(aConfigContext);
    }

    public void handle(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        httpServletRequest = new ForbidSessionCreationWrapper(httpServletRequest);
        String query = httpServletRequest.getQueryString();
        if (query != null) {
            if (HttpUtils.indexOfIngnoreCase(query , "wsdl2") > 0 || HttpUtils.indexOfIngnoreCase(query, "wsdl") > 0 ||
                HttpUtils.indexOfIngnoreCase(query, "xsd") > 0 || HttpUtils.indexOfIngnoreCase(query, "policy") > 0) {
                processListService(httpServletRequest, httpServletResponse);
            } else {
                super.handle(httpServletRequest, httpServletResponse);
            }
        } else {
            super.handle(httpServletRequest, httpServletResponse);
        }
    }

    protected void processListFaultyServices(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String serviceName = req.getParameter("serviceName");
        if (serviceName != null) {
            AxisService service = configContext.getAxisConfiguration().getService(serviceName);
            req.setAttribute(Constants.SINGLE_SERVICE, service);
        }
        renderView(LIST_FAULTY_SERVICES_JSP_NAME, req, res);
    }


    protected void processIndex(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        processListServices(httpServletRequest, httpServletResponse);
    }

    private String extractHost(String filePart) {
        return WSDLSerializationUtil.extractHostIP(filePart);
    }

    public void processExplicitSchemaAndWSDL(HttpServletRequest req,
                                             HttpServletResponse res)
            throws IOException, ServletException {
        HashMap<String, AxisService> services = configContext.getAxisConfiguration().getServices();
        String filePart = req.getRequestURL().toString();
        String schema = filePart.substring(filePart.lastIndexOf("/") + 1,
                                           filePart.length());
        if ((services != null) && !services.isEmpty()) {
            Iterator<AxisService> i = services.values().iterator();
            while (i.hasNext()) {
                AxisService service = (AxisService) i.next();
                InputStream stream = HTTPTransportUtils.getMetaInfResourceAsStream(service, schema);
                if (stream != null) {
                    OutputStream out = res.getOutputStream();
                    res.setContentType("text/xml");
                    IOUtils.copy(stream, out, true);
                    return;
                }
            }
        }
    }

    /**
     * Copies the input stream to the output stream
     *
     * @param stream  the <code>InputStream</code>
     * @param ostream the <code>OutputStream</code>
     */
    public static void copy(InputStream stream, OutputStream ostream) throws IOException {
        IOUtils.copy(stream, ostream, false);
    }

    public String extractServiceName(String urlString) {
        int n = urlString.indexOf(configContext.getServiceContextPath());
        if (n != -1) {
            String serviceName = urlString.substring(n + configContext.getServiceContextPath().length(),
                    urlString.length());
            if (serviceName.length() > 0) {
                if(serviceName.charAt(0)=='/'){
                    serviceName = serviceName.substring(1);
                }
                return serviceName;
            }
        }
        return urlString.substring(urlString.lastIndexOf("/") + 1,
                urlString.length());
    }

    public void processListService(HttpServletRequest req,
                                   HttpServletResponse res)
            throws IOException, ServletException {

        String url = req.getRequestURL().toString();
        String serviceName = extractServiceName(url);
        HashMap<String, AxisService> services = configContext.getAxisConfiguration().getServices();
        String query = req.getQueryString();
        int wsdl2 = HttpUtils.indexOfIngnoreCase(query, "wsdl2");
        int wsdl = HttpUtils.indexOfIngnoreCase(query, "wsdl");
        int xsd = HttpUtils.indexOfIngnoreCase(query, "xsd");
        int policy = HttpUtils.indexOfIngnoreCase(query, "policy");

        if ((services != null) && !services.isEmpty()) {
            AxisService axisService = services.get(serviceName);
            if (axisService != null) {              
                if (wsdl2 >= 0) {
                    handleWSDL2Request(req, res, url, axisService);
                    return;
                } else if (wsdl >= 0) {
                    handleWSDLRequest(req, res, url, axisService);
                    return;
                } else if (xsd >= 0) {
                    handleXSDRequest(req, res, axisService);
                    return;
                } else if (policy >= 0) {
                    handlePolicyRequest(req, res, serviceName, axisService);
                    return;
                }
            }
        }
        res.sendError(HttpServletResponse.SC_NOT_FOUND, url);
    }

    private void handlePolicyRequest(HttpServletRequest req,
                                     HttpServletResponse res,
                                     String serviceName,
                                     AxisService axisService) throws IOException, ServletException {
        if (!canExposeServiceMetadata(axisService)){
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        ExternalPolicySerializer serializer = new ExternalPolicySerializer();
        serializer.setAssertionsToFilter(configContext
                .getAxisConfiguration().getLocalPolicyAssertions());

        // check whether Id is set
        String idParam = req.getParameter("id");

        if (idParam != null) {
            // Id is set

            Policy targetPolicy = findPolicy(idParam, axisService);

            if (targetPolicy != null) {
                XMLStreamWriter writer;

                try {
                    OutputStream out = res.getOutputStream();
                    writer = XMLOutputFactory.newInstance()
                            .createXMLStreamWriter(out);

                    res.setContentType("application/wspolicy+xml");
                    targetPolicy.serialize(writer);
                    writer.flush();

                } catch (XMLStreamException e) {
                    throw new ServletException(
                            "Error occured when serializing the Policy",
                            e);

                } catch (FactoryConfigurationError e) {
                    throw new ServletException(
                            "Error occured when serializing the Policy",
                            e);
                }

            } else {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } else {

            PolicyInclude policyInclude = axisService.getPolicyInclude();
            Policy effecPolicy = policyInclude.getEffectivePolicy();

            if (effecPolicy != null) {
                XMLStreamWriter writer;

                try {
                    OutputStream out = res.getOutputStream();
                    writer = XMLOutputFactory.newInstance()
                            .createXMLStreamWriter(out);

                    res.setContentType("application/wspolicy+xml");
                    effecPolicy.serialize(writer);
                    writer.flush();

                } catch (XMLStreamException e) {
                    throw new ServletException(
                            "Error occured when serializing the Policy",
                            e);

                } catch (FactoryConfigurationError e) {
                    throw new ServletException(
                            "Error occured when serializing the Policy",
                            e);
                }
            } else {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    private void handleXSDRequest(HttpServletRequest req, HttpServletResponse res,
                                  AxisService axisService) throws IOException {
        if (!canExposeServiceMetadata(axisService)){
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        res.setContentType("text/xml");
        int ret = axisService.printXSD(res.getOutputStream(), getParamtereIgnoreCase(req ,"xsd"));
        if (ret == 0) {
            //multiple schemas are present and the user specified
            //no name - in this case we cannot possibly pump a schema
            //so redirect to the service root
            res.sendRedirect("");
        } else if (ret == -1) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleWSDLRequest(HttpServletRequest req,
                                   HttpServletResponse res,
                                   String url,
                                   AxisService axisService) throws IOException {
        if (!canExposeServiceMetadata(axisService)){
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        OutputStream out = res.getOutputStream();
        res.setContentType("text/xml");
        String ip = extractHost(url);
        String wsdlName = getParamtereIgnoreCase(req , "wsdl");

        if (wsdlName != null && wsdlName.length()>0) {
            axisService.printUserWSDL(out, wsdlName, ip);
        } else {
            axisService.printWSDL(out, ip);
        }
    }

    private void handleWSDL2Request(HttpServletRequest req,
                                    HttpServletResponse res,
                                    String url,
                                    AxisService axisService) throws IOException {
        if (!canExposeServiceMetadata(axisService)){
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        res.setContentType("text/xml");
        String ip = extractHost(url);
        String wsdlName = getParamtereIgnoreCase(req , "wsdl2");

        int ret = axisService.printWSDL2(res.getOutputStream(), ip, wsdlName);
        if (ret == 0) {
            res.sendRedirect("");
        } else if (ret == -1) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public String getParamtereIgnoreCase(HttpServletRequest req ,String paraName){
        Enumeration e = req.getParameterNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            if(name.equalsIgnoreCase(paraName)) {
                String value = req.getParameter(name);
                return value;
            }

        }
        return null;
    }

    /**
     * Checks whether exposing the WSDL & WSDL elements such as schema & policy have been allowed
     *
     * @param service  The AxisService which needs to be verified
     * @throws IOException If exposing WSDL & WSDL elements has been restricted.
     * @return true - if service metadata can be exposed, false - otherwise
     */
    private boolean canExposeServiceMetadata(AxisService service) {
        Parameter exposeServiceMetadata = service.getParameter("exposeServiceMetadata");
        if(exposeServiceMetadata != null &&
           JavaUtils.isFalseExplicitly(exposeServiceMetadata.getValue())) {
           return false;
        }
        return true;
    }

    protected void processListServices(HttpServletRequest req,
                                       HttpServletResponse res)
            throws IOException, ServletException {
        if(listServiceDisabled()){
           return;
        }
        populateRequestAttributes(req);
        req.setAttribute(Constants.ERROR_SERVICE_MAP,
                configContext.getAxisConfiguration().getFaultyServices());
        renderView(LIST_MULTIPLE_SERVICE_JSP_NAME, req, res);
    }

    private Policy findPolicy(String id, AxisDescription des) {

       
        Collection<PolicyComponent> policyElements = des.getPolicySubject().getAttachedPolicyComponents();
        PolicyRegistry registry = new PolicyRegistryImpl();

        Object policyComponent;

        Policy policy = registry.lookup(id);

        if (policy != null) {
            return policy;
        }

        for (Iterator<PolicyComponent> iterator = policyElements.iterator(); iterator.hasNext();) {
            policyComponent = iterator.next();

            if (policyComponent instanceof Policy) {
                // policy found for the id

                if (id.equals(((Policy) policyComponent).getId())) {
                    return (Policy) policyComponent;
                }
            }
        }

        AxisDescription child;

        for (Iterator<? extends AxisDescription> iterator = des.getChildren(); iterator.hasNext();) {
            child = (AxisDescription) iterator.next();
            policy = findPolicy(id, child);

            if (policy != null) {
                return policy;
            }
        }

        return null;
    }

    private boolean listServiceDisabled () {
        Parameter parameter = configContext.getAxisConfiguration()
                .getParameter(Constants.ADMIN_SERVICE_LISTING_DISABLED);
          return parameter != null && "true".equals(parameter.getValue());
    }

}
