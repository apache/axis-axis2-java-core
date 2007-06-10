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

import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.util.ExternalPolicySerializer;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyRegistry;
import org.apache.ws.commons.schema.XmlSchema;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class ListingAgent extends AbstractAgent {

    private static final String LIST_MULTIPLE_SERVICE_JSP_NAME =
            "listServices.jsp";
    private static final String LIST_SINGLE_SERVICE_JSP_NAME =
            "listSingleService.jsp";
    private static final String LIST_FAULTY_SERVICES_JSP_NAME = "listFaultyService.jsp";

    public static final String RUNNING_PORT = "RUNNING_PORT";

    public ListingAgent(ConfigurationContext aConfigContext) {
        super(aConfigContext);
    }

    private void addTransportListner(String schema, int port) {
        try {
            TransportInDescription trsIn =
                    configContext.getAxisConfiguration().getTransportIn(schema);
            if (trsIn == null) {
                trsIn = new TransportInDescription(schema);
                HTTPSListener httspReceiver = new HTTPSListener(port, schema);
                httspReceiver.init(configContext, trsIn);
                trsIn.setReceiver(httspReceiver);
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
        String query = httpServletRequest.getQueryString();
        if (query != null) {
            if (query.indexOf("?wsdl2") > 0 || query.indexOf("?wsdl") > 0 ||
                query.indexOf("?xsd") > 0) {
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
            req.getSession().setAttribute(Constants.SINGLE_SERVICE, service);
        }
        renderView(LIST_FAULTY_SERVICES_JSP_NAME, req, res);
    }


    protected void processIndex(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        processListServices(httpServletRequest, httpServletResponse);
    }

    private String extractHostAndPort(String filePart, boolean isHttp) {
        int ipindex = filePart.indexOf("//");
        String ip = null;
        if (ipindex >= 0) {
            ip = filePart.substring(ipindex + 2, filePart.length());
            int seperatorIndex = ip.indexOf(":");
            int slashIndex = ip.indexOf("/");
            String port;
            if (seperatorIndex >= 0) {
                port = ip.substring(seperatorIndex + 1, slashIndex);
                ip = ip.substring(0, seperatorIndex);
            } else {
                ip = ip.substring(0, slashIndex);
                port = "80";
            }
            if (isHttp) {
                configContext.setProperty(RUNNING_PORT, port);
            }
        }
        return ip;
    }

    public void processExplicitSchemaAndWSDL(HttpServletRequest req,
                                             HttpServletResponse res)
            throws IOException, ServletException {
        HashMap services = configContext.getAxisConfiguration().getServices();
        String filePart = req.getRequestURL().toString();
        String schema = filePart.substring(filePart.lastIndexOf("/") + 1,
                                           filePart.length());
        if ((services != null) && !services.isEmpty()) {
            Iterator i = services.values().iterator();
            while (i.hasNext()) {
                AxisService service = (AxisService) i.next();
                InputStream stream = service.getClassLoader().getResourceAsStream("META-INF/" + schema);
                if (stream != null) {
                    OutputStream out = res.getOutputStream();
                    res.setContentType("text/xml");
                    copy(stream, out);
                    out.flush();
                    out.close();
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
        int nextValue = stream.read();
        while (-1 != nextValue) {
            ostream.write(nextValue);
            nextValue = stream.read();
        }
    }

    public void processListService(HttpServletRequest req,
                                   HttpServletResponse res)
            throws IOException, ServletException {

        String filePart = req.getRequestURL().toString();
        String serviceName = filePart.substring(filePart.lastIndexOf("/") + 1,
                                                filePart.length());
        HashMap services = configContext.getAxisConfiguration().getServices();
        String query = req.getQueryString();
        int wsdl2 = query.indexOf("wsdl2");
        int wsdl = query.indexOf("wsdl");
        int xsd = query.indexOf("xsd");
        int policy = query.indexOf("policy");

        if ((services != null) && !services.isEmpty()) {
            Object serviceObj = services.get(serviceName);
            if (serviceObj != null) {
                boolean isHttp = "http".equals(req.getScheme());
                if (wsdl2 >= 0) {
                    OutputStream out = res.getOutputStream();
                    res.setContentType("text/xml");
                    String ip = extractHostAndPort(filePart, isHttp);
                    ((AxisService) serviceObj)
                            .printWSDL2(out);
                    out.flush();
                    out.close();
                    return;
                } else if (wsdl >= 0) {
                    OutputStream out = res.getOutputStream();
                    res.setContentType("text/xml");
                    String ip = extractHostAndPort(filePart, isHttp);
                    ((AxisService) serviceObj).printWSDL(out, ip);
                    out.flush();
                    out.close();
                    return;
                } else if (xsd >= 0) {
                    OutputStream out = res.getOutputStream();
                    res.setContentType("text/xml");
                    AxisService axisService = (AxisService) serviceObj;
                    //call the populator
                    axisService.populateSchemaMappings();
                    Map schemaMappingtable =
                            axisService.getSchemaMappingTable();
                    ArrayList schemas = axisService.getSchema();

                    //a name is present - try to pump the requested schema
                    String xsds = req.getParameter("xsd");
                    if (!"".equals(xsds)) {
                        XmlSchema schema =
                                (XmlSchema) schemaMappingtable.get(xsds);
                        if (schema != null) {
                            //schema is there - pump it outs
                            schema.write(new OutputStreamWriter(out, "UTF8"));
                            out.flush();
                            out.close();
                        } else {
                            InputStream in = axisService.getClassLoader()
                                    .getResourceAsStream(DeploymentConstants.META_INF + "/" + xsds);
                            if (in != null) {
                                out.write(IOUtils.getStreamAsByteArray(in));
                                out.flush();
                                out.close();
                            } else {
                                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                            }
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
                            schema.write(new OutputStreamWriter(out, "UTF8"));
                            out.flush();
                            out.close();
                        }
                    }
                    return;
                } else if (policy >= 0) {

                    OutputStream out = res.getOutputStream();

                    ExternalPolicySerializer serializer = new ExternalPolicySerializer();
                    serializer.setAssertionsToFilter(configContext
                            .getAxisConfiguration().getLocalPolicyAssertions());

                    // check whether Id is set
                    String idParam = req.getParameter("id");

                    if (idParam != null) {
                        // Id is set

                        Policy targetPolicy = findPolicy(idParam, (AxisService) serviceObj);

                        if (targetPolicy != null) {
                            XMLStreamWriter writer;

                            try {
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

                            res.setContentType("text/html");
                            String outStr = "<b>No policy found for id="
                                            + idParam + "</b>";
                            out.write(outStr.getBytes());
                        }

                    } else {

                        PolicyInclude policyInclude = ((AxisService) serviceObj).getPolicyInclude();
                        Policy effecPolicy = policyInclude.getEffectivePolicy();

                        if (effecPolicy != null) {
                            XMLStreamWriter writer;

                            try {
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

                            res.setContentType("text/html");
                            String outStr = "<b>No effective policy for "
                                            + serviceName + " servcie</b>";
                            out.write(outStr.getBytes());
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

        populateSessionInformation(req);
        req.getSession().setAttribute(Constants.ERROR_SERVICE_MAP,
                                      configContext.getAxisConfiguration().getFaultyServices());

        renderView(LIST_MULTIPLE_SERVICE_JSP_NAME, req, res);
    }

    private Policy findPolicy(String id, AxisDescription des) {

        List policyElements = des.getPolicyInclude().getPolicyElements();
        PolicyRegistry registry = des.getPolicyInclude().getPolicyRegistry();

        Object policyComponent;

        Policy policy = registry.lookup(id);

        if (policy != null) {
            return policy;
        }

        for (Iterator iterator = policyElements.iterator(); iterator.hasNext();) {
            policyComponent = iterator.next();

            if (policyComponent instanceof Policy) {
                // policy found for the id

                if (id.equals(((Policy) policyComponent).getId())) {
                    return (Policy) policyComponent;
                }
            }
        }

        AxisDescription child;

        for (Iterator iterator = des.getChildren(); iterator.hasNext();) {
            child = (AxisDescription) iterator.next();
            policy = findPolicy(id, child);

            if (policy != null) {
                return policy;
            }
        }

        return null;
    }

    /**
     * This class is just to add tarnsport at the runtime if user send requet using
     * diffrent schemes , simly to handle http/https seperetaly
     */
    private class HTTPSListener implements TransportListener {

        private int port;
        private String schema;
        private ConfigurationContext axisConf;

        public HTTPSListener(int port, String schema) {
            this.port = port;
            this.schema = schema;
        }

        public void init(ConfigurationContext axisConf,
                         TransportInDescription transprtIn) throws AxisFault {
            this.axisConf = axisConf;
        }

        public void start() throws AxisFault {
        }

        public void stop() throws AxisFault {
        }

        public EndpointReference[] getEPRsForService(String serviceName, String ip)
                throws AxisFault {
            return new EndpointReference[]{new EndpointReference(schema + "://" + ip + ":" + port +
                                                                 "/" + axisConf.getServiceContextPath() + "/" +
                                                                 serviceName)};  //To change body of implemented methods use File | Settings | File Templates.
        }

        public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
            return getEPRsForService(serviceName, ip)[0];
        }

        public SessionContext getSessionContext(MessageContext messageContext) {
            HttpServletRequest req = (HttpServletRequest) messageContext.getProperty(
                    HTTPConstants.MC_HTTP_SERVLETREQUEST);
            SessionContext sessionContext =
                    (SessionContext) req.getSession(true).getAttribute(
                            Constants.SESSION_CONTEXT_PROPERTY);
            String sessionId = req.getSession().getId();
            if (sessionContext == null) {
                sessionContext = new SessionContext(null);
                sessionContext.setCookieID(sessionId);
                req.getSession().setAttribute(Constants.SESSION_CONTEXT_PROPERTY,
                                              sessionContext);
            }
            messageContext.setSessionContext(sessionContext);
            messageContext.setProperty(AxisServlet.SESSION_ID, sessionId);
            return sessionContext;
        }

        public void destroy() {
        }

    }

}
