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
package org.apache.axis.transport.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.Constants;
import org.apache.axis.deployment.util.DeploymentData;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.AxisConfigurationImpl;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.soap.impl.llom.soap11.SOAP11Factory;

/**
 * Class AxisServlet
 */
public class AxisServlet extends HttpServlet {
    /**
     * Field engineRegistry
     */

    private ConfigurationContext engineContext;

    /**
     * Field LIST_MULTIPLE_SERVICE_JSP_NAME
     */
    private static final String LIST_MULTIPLE_SERVICE_JSP_NAME =
            "listServices.jsp";

    private static final String LIST_SRVICES_JSP_NAME =
            "listService.jsp";

    private static final String ADMIN_JSP_NAME =
            "admin.jsp";



    private static final String LIST_AVAILABLE_MODULES_JSP_NAME =
            "listModules.jsp";

    private static final String LIST_GLOABLLY_ENGAGED_MODULES_JSP_NAME =
            "globalModules.jsp";

    private static final String LIST_PHASES_JSP_NAME =
            "viewphases.jsp";

    private static final String ENGAGING_MODULE_GLOBALLY_JSP_NAME =
            "engagingglobally.jsp";

    private static final String ENGAGING_MODULE_TO_SERVICE_JSP_NAME =
            "engagingtoaservice.jsp";

    /**
     * Field LIST_SINGLE_SERVICE_JSP_NAME
     */
    private static final String LIST_SINGLE_SERVICE_JSP_NAME =
            "listSingleService.jsp";


    /**
     * Field allowListServices
     */
    private final boolean allowListServices = true;

    /**
     * Field allowListSingleService
     */
    private final boolean allowListSingleService = true;

    /**
     * Method init
     *
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        try {
            ServletContext context = config.getServletContext();
            String repoDir = context.getRealPath("/WEB-INF");
            ConfigurationContextFactory erfac =
                    new ConfigurationContextFactory();
            engineContext = erfac.buildEngineContext(repoDir);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Method doGet
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        String filePart = httpServletRequest.getRequestURL().toString();
        if((filePart != null)
                && filePart.endsWith(Constants.ADMIN_LISTSERVICES)){
            listAdminServices(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null)
                && filePart.endsWith(Constants.LIST_MODULES)){
            listModules(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null)
                && filePart.endsWith(Constants.LIST_GLOABLLY_ENGAGED_MODULES)){
            listGloballyModules(httpServletRequest, httpServletResponse);
            return;
        }  else if ((filePart != null)
                && filePart.endsWith(Constants.LIST_PHASES)){
            listPhases(httpServletRequest, httpServletResponse);
            return;
        }else if ((filePart != null)
                && filePart.endsWith(Constants.ENGAGE_GLOBAL_MODULE)){
            engageModulesGlobally(httpServletRequest, httpServletResponse);
            return;
        }
        else if ((filePart != null)
                && filePart.endsWith(Constants.ENGAGE_MODULE_TO_SERVICE)){
            engageModulesToService(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null)
                && filePart.endsWith(Constants.ADMIN_LOGGING)){
            adminLogging(httpServletRequest, httpServletResponse);
            return;
        }


        if (allowListServices
                && (filePart != null)
                && filePart.endsWith(Constants.LISTSERVICES)) {
            listServices(httpServletRequest, httpServletResponse);
            return;
        } else {
            if (allowListSingleService) {
                listService(httpServletRequest, httpServletResponse, filePart);
                return;
            }
        }
    }

    /*
    * (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */

    /**
     * Method doPost
     *
     * @param req
     * @param res
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            res.setContentType("text/xml; charset=utf-8");
            AxisEngine engine = new AxisEngine(engineContext);
            Object sessionContext =
                    req.getSession().getAttribute(
                            Constants.SESSION_CONTEXT_PROPERTY);
            if (sessionContext == null) {
                sessionContext = new SessionContext(null);
                req.getSession().setAttribute(
                        Constants.SESSION_CONTEXT_PROPERTY,
                        sessionContext);
            }
            MessageContext msgContext =
                    new MessageContext(engineContext,
                            (SessionContext) sessionContext,
                            engineContext.getAxisConfiguration().getTransportIn(
                                    new QName(Constants.TRANSPORT_HTTP)),
                            engineContext.getAxisConfiguration().getTransportOut(
                                    new QName(Constants.TRANSPORT_HTTP)));
            msgContext.setServerSide(true);
            String filePart = req.getRequestURL().toString();
            msgContext.setTo(
                    new EndpointReference(AddressingConstants.WSA_TO, filePart));
            String soapActionString =
                    req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
            if (soapActionString != null) {
                msgContext.setWSAAction(soapActionString);
            }
            XMLStreamReader reader =
                    XMLInputFactory.newInstance().createXMLStreamReader(
                            new BufferedReader(
                                    new InputStreamReader(req.getInputStream())));

            //Check for the REST behaviour, if you desire rest beahaviour
            //put a <parameter name="doREST" value="true"/> at the server.xml/client.xml file
            Object doREST = msgContext.getProperty(Constants.DO_REST);
            StAXBuilder builder = null;
            SOAPEnvelope envelope = null;
            if (doREST != null && "true".equals(doREST)) {
                SOAPFactory soapFactory = new SOAP11Factory();
                builder = new StAXOMBuilder(reader);
                builder.setOmbuilderFactory(soapFactory);
                envelope = soapFactory.getDefaultEnvelope();
                envelope.getBody().addChild(builder.getDocumentElement());
            } else {
                builder = new StAXSOAPModelBuilder(reader);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            }

            msgContext.setEnvelope(envelope);

            msgContext.setProperty(
                    MessageContext.TRANSPORT_WRITER,
                    new BufferedWriter(res.getWriter()));
            engine.receive(msgContext);
        } catch (AxisFault e) {
            throw new ServletException(e);
        } catch (XMLStreamException e) {
            throw new ServletException(e);
        } catch (FactoryConfigurationError e) {
            throw new ServletException(e);
        }
    }

    /**
     * Method listServices
     *
     * @param req
     * @param res
     * @throws IOException
     */
    private void listServices(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap services = engineContext.getAxisConfiguration().getServices();
        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(
                Constants.ERROR_SERVICE_MAP,
                engineContext.getAxisConfiguration().getFaulytServices());
        res.sendRedirect(LIST_MULTIPLE_SERVICE_JSP_NAME);
    }

    /**
     *
     * @param req
     * @param res
     * @throws IOException
     */
    private void listAdminServices(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap services = engineContext.getAxisConfiguration().getServices();
        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(
                Constants.ERROR_SERVICE_MAP,
                engineContext.getAxisConfiguration().getFaulytServices());
        res.sendRedirect(LIST_SRVICES_JSP_NAME);
    }
    private void adminLogging(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        String username = req.getParameter("userName");
        String password = req.getParameter("password");
        if(username == null || password == null || username.trim().equals("") || password.trim().equals("")){
            throw new AxisFault("invalid user name");
        }
        String adminUserName =(String)((AxisConfigurationImpl) engineContext.getAxisConfiguration())
                .getParameter(Constants.USER_NAME).getValue();
        String adminPassword =(String)((AxisConfigurationImpl) engineContext.getAxisConfiguration())
                .getParameter(Constants.PASSWORD).getValue();
        if(username!= null && password !=null && username.equals(adminUserName) &&
                password.equals(adminPassword)){
            req.getSession().setAttribute(Constants.LOGGED, "Yes");
            res.sendRedirect(ADMIN_JSP_NAME);
        } else {
            throw new AxisFault("invalid user name");
        }
    }

    private void listModules(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap modules =((AxisConfigurationImpl) engineContext.getAxisConfiguration()).getModules();
        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        res.sendRedirect(LIST_AVAILABLE_MODULES_JSP_NAME);
    }

    private void engageModulesGlobally(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap modules =((AxisConfigurationImpl) engineContext.getAxisConfiguration()).getModules();
        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        String moduleName =(String)req.getParameter("modules");
        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        if(moduleName !=null){
            try {
                engineContext.getAxisConfiguration().engageModule(new QName(moduleName));
                req.getSession().setAttribute(Constants.ENGAGE_STATUS, moduleName + " module engaged globally Successfully");
            } catch (AxisFault axisFault) {
                req.getSession().setAttribute(Constants.ENGAGE_STATUS, axisFault.getMessage());
            }
        }
        req.getSession().setAttribute("modules",null);
        res.sendRedirect(ENGAGING_MODULE_GLOBALLY_JSP_NAME);
    }

    private void engageModulesToService(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap modules =((AxisConfigurationImpl) engineContext.getAxisConfiguration()).getModules();
        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        HashMap services = engineContext.getAxisConfiguration().getServices();
        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        String moduleName =(String)req.getParameter("modules");
        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        req.getSession().setAttribute("modules",null);
        String serviceName =(String)req.getParameter("service");
        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        if(serviceName !=null && moduleName !=null){
            try {

                engineContext.getAxisConfiguration().getService(new QName(serviceName)).engageModule(
                        engineContext.getAxisConfiguration().getModule(new QName(moduleName)));
                req.getSession().setAttribute(Constants.ENGAGE_STATUS, moduleName +
                        " module engaged to the service Successfully");
            } catch (AxisFault axisFault) {
                req.getSession().setAttribute(Constants.ENGAGE_STATUS, axisFault.getMessage());
            }
        }
        req.getSession().setAttribute("service",null);
        res.sendRedirect(ENGAGING_MODULE_TO_SERVICE_JSP_NAME);
    }

    private void listGloballyModules(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        Collection modules =((AxisConfigurationImpl) engineContext.getAxisConfiguration()).getEngadgedModules();
        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        res.sendRedirect(LIST_GLOABLLY_ENGAGED_MODULES_JSP_NAME);
    }

    private void listPhases(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        ArrayList phaselist = new ArrayList();
        DeploymentData depdata = DeploymentData.getInstance();
        phaselist.add(depdata.getINPhases());
        phaselist.add(depdata.getIN_FaultPhases());
        phaselist.add(depdata.getOUTPhases());
        phaselist.add(depdata.getOUT_FaultPhases());

        phaselist.add(depdata.getOperationInPhases());
        phaselist.add(depdata.getOperationInFaultPhases());
        phaselist.add(depdata.getOperationOutPhases());
        phaselist.add(depdata.getOperationOutFaultPhases());

        req.getSession().setAttribute(Constants.PHASE_LIST, phaselist);
        res.sendRedirect(LIST_PHASES_JSP_NAME);
    }


    /**
     * Method listService
     *
     * @param req
     * @param res
     * @param filePart
     * @throws IOException
     */
    private void listService(
            HttpServletRequest req,
            HttpServletResponse res,
            String filePart)
            throws IOException {
        String serviceName =
                filePart.substring(
                        filePart.lastIndexOf("/") + 1,
                        filePart.length());
        HashMap services = engineContext.getAxisConfiguration().getServices();
        if ((services != null) && !services.isEmpty()) {
            Object serviceObj = services.get(new QName(serviceName));
            if (serviceObj != null) {
                req.getSession().setAttribute(
                        Constants.SINGLE_SERVICE,
                        serviceObj);
            }
        }
        String URI = req.getRequestURI();
        URI = URI.substring(0, URI.indexOf("services"));
        res.sendRedirect(URI + LIST_SINGLE_SERVICE_JSP_NAME);
    }
}
