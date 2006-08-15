/*
* Copyright 2004,2006 The Apache Software Foundation.
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
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.ws.commons.schema.XmlSchema;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class ListingAgent extends AbstractAgent {

    private static final String LIST_MULTIPLE_SERVICE_JSP_NAME =
            "listServices.jsp";
    private static final String LIST_SINGLE_SERVICE_JSP_NAME =
            "listSingleService.jsp";
    private static final String LIST_FAULTY_SERVICES_JSP_NAME = "listFaultyService.jsp";

    public static final String RUNNING_PORT = "RUNNING_PORT";
    private String servicePath;

    public ListingAgent(ConfigurationContext aConfigContext) {
        super(aConfigContext);
        servicePath = aConfigContext.getServicePath();
    }

    private void addTransportListner(String schema, int port) {
        try {
            TransportInDescription trsIn =
                    configContext.getAxisConfiguration().getTransportIn(
                            new QName(schema));
            if (trsIn == null) {
                trsIn = new TransportInDescription(new QName(schema));
                trsIn.setReceiver(new HTTPSTListener(port, schema));
                configContext.getListenerManager().addListener(trsIn, true);
            }
        } catch (AxisFault axisFault) {
            //
        }
    }

    public void handle(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        // httpServletRequest.getLocalPort() , giving me a build error so I had to use the followin
        String filePart = httpServletRequest.getRequestURL().toString();
        int ipindex = filePart.indexOf("//");
        String ip;
        if (ipindex >= 0) {
            ip = filePart.substring(ipindex + 2, filePart.length());
            int seperatorIndex = ip.indexOf(":");
            int slashIndex = ip.indexOf("/");
            String portstr = ip.substring(seperatorIndex + 1,
                    slashIndex);
            try {
                addTransportListner(httpServletRequest.getScheme(), Integer.parseInt(portstr));
            } catch (NumberFormatException e) {
                //
            }
        }
        if (httpServletRequest.getParameter("wsdl") != null ||
             httpServletRequest.getParameter("wsdl2") != null ||
                httpServletRequest.getParameter("xsd") != null) {
            processListService(httpServletRequest, httpServletResponse);
        } else {
            super.handle(httpServletRequest, httpServletResponse);
        }
    }

    protected void processListFaultyServices(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String serviceName = req.getParameter("serviceName");
        if (serviceName != null) {
            AxisService service = configContext.getAxisConfiguration().getService(serviceName);
            req.getSession().setAttribute(Constants.SINGLE_SERVICE, service);
        }
        renderView(LIST_FAULTY_SERVICES_JSP_NAME, req, res);
    }


    protected void processIndex(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        processListServices(httpServletRequest, httpServletResponse);
    }

    protected void processListService(HttpServletRequest req,
                                      HttpServletResponse res)
            throws IOException, ServletException {

        String filePart = req.getRequestURL().toString();
        String serviceName = filePart.substring(filePart.lastIndexOf("/") + 1,
                filePart.length());
        HashMap services = configContext.getAxisConfiguration().getServices();
        String wsdl = req.getParameter("wsdl");
        String wsdl2 = req.getParameter("wsdl2");
        String xsd = req.getParameter("xsd");
        if ((services != null) && !services.isEmpty()) {
            Object serviceObj = services.get(serviceName);
            if (serviceObj != null) {
                if (wsdl != null) {
                    OutputStream out = res.getOutputStream();
                    res.setContentType("text/xml");
                    int ipindex = filePart.indexOf("//");
                    String ip = null;
                    if (ipindex >= 0) {
                        ip = filePart.substring(ipindex + 2, filePart.length());
                        int seperatorIndex = ip.indexOf(":");
                        int slashIndex = ip.indexOf("/");
                        String port = ip.substring(seperatorIndex + 1,
                                slashIndex);
                        if ("http".equals(req.getScheme())) {
                            configContext.setProperty(RUNNING_PORT, port);
                        }
                        if (seperatorIndex > 0) {
                            ip = ip.substring(0, seperatorIndex);
                        }
                    }
                    ((AxisService) serviceObj).printWSDL(out, ip, servicePath);
                    out.flush();
                    out.close();
                    return;
                } else if (wsdl2 != null) {
                    OutputStream out = res.getOutputStream();
                    res.setContentType("text/xml");
                    int ipindex = filePart.indexOf("//");
                    String ip = null;
                    if (ipindex >= 0) {
                        ip = filePart.substring(ipindex + 2, filePart.length());
                        int seperatorIndex = ip.indexOf(":");
                        int slashIndex = ip.indexOf("/");
                        String port = ip.substring(seperatorIndex + 1,
                                slashIndex);
                        if ("http".equals(req.getScheme())) {
                            configContext.setProperty(RUNNING_PORT, port);
                        }
                        if (seperatorIndex > 0) {
                            ip = ip.substring(0, seperatorIndex);
                        }
                    }
                    ((AxisService) serviceObj).printWSDL2(out, ip, servicePath);
                    out.flush();
                    out.close();
                    return;
                } else if (xsd != null) {
                    OutputStream out = res.getOutputStream();
                    res.setContentType("text/xml");
                    AxisService axisService = (AxisService) serviceObj;
                    //call the populator
                    axisService.populateSchemaMappings();
                    Hashtable schemaMappingtable =
                            axisService.getSchemaMappingTable();
                    ArrayList schemas = axisService.getSchema();

                    //a name is present - try to pump the requested schema
                    if (!"".equals(xsd)) {
                        XmlSchema schema =
                                (XmlSchema) schemaMappingtable.get(xsd);
                        if (schema != null) {
                            //schema is there - pump it outs
                            schema.write(out);
                            out.flush();
                            out.close();
                        } else {
                            //the schema is not found - pump a 404
                            res.sendError(HttpServletResponse.SC_NOT_FOUND);
                        }

                        //multiple schemas are present and the user specified
                        //no name - in this case we cannot possibly pump a schema
                        //so redirect to the service root
                    } else if (schemas.size() > 1) {
                        res.sendRedirect("");
                        //user specified no name and there is only one schema
                        //so pump that out
                    } else {
                        XmlSchema schema = axisService.getSchema(0);
                        if (schema != null) {
                            schema.write(out);
                            out.flush();
                            out.close();
                        }
                    }
                    return;
                } else {
                    req.getSession().setAttribute(Constants.SINGLE_SERVICE,
                            serviceObj);
                }
            } else {
                req.getSession().setAttribute(Constants.SINGLE_SERVICE, null);
            }
        }

        renderView(LIST_SINGLE_SERVICE_JSP_NAME, req, res);
    }

    protected void processListServices(HttpServletRequest req,
                                       HttpServletResponse res)
            throws IOException, ServletException {
        HashMap services = configContext.getAxisConfiguration().getServices();

        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.ERROR_SERVICE_MAP,
                configContext.getAxisConfiguration().getFaultyServices());

        renderView(LIST_MULTIPLE_SERVICE_JSP_NAME, req, res);
    }

    /**
     * This class is just to add tarnsport at the runtime if user send requet using
     * diffrent schemes , simly to handle http/https seperetaly
     */
    private class HTTPSTListener implements TransportListener {

        private int port;
        private String schema;
        private String contextPath;

        public HTTPSTListener(int port, String schema) {
            this.port = port;
            this.schema = schema;
        }

        public void init(ConfigurationContext axisConf,
                         TransportInDescription transprtIn) throws AxisFault {
            contextPath = axisConf.getContextPath();
        }

        public void start() throws AxisFault {
        }

        public void stop() throws AxisFault {
        }

        public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
            return new EndpointReference(schema + "://" + ip + ":" + port + contextPath + "/" + serviceName);
        }
    }

}
