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


package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.server.AdminAppException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ListingAgent {

    /**
     * Field LIST_MULTIPLE_SERVICE_JSP_NAME
     */
    private static final String LIST_MULTIPLE_SERVICE_JSP_NAME = "listServices.jsp";
    private static final String LIST_SERVICE_GROUP_JSP = "ListServiceGroup.jsp";
    private static final String LIST_SERVICES_JSP_NAME = "listService.jsp";
    private static final String SELECT_SERVICE_JSP_NAME = "SelectService.jsp";
    private static final String IN_ACTIVATE_SERVICE_JSP_NAME = "InActivateService.jsp";
    private static final String ACTIVATE_SERVICE_JSP_NAME = "ActivateService.jsp";

    /**
     * Field LIST_SINGLE_SERVICE_JSP_NAME
     */
    private static final String LIST_SINGLE_SERVICE_JSP_NAME = "listSingleService.jsp";
    private static final String LIST_PHASES_JSP_NAME = "viewphases.jsp";
    private static final String LIST_GLOABLLY_ENGAGED_MODULES_JSP_NAME = "globalModules.jsp";
    private static final String LIST_AVAILABLE_MODULES_JSP_NAME = "listModules.jsp";
    private static final String ENGAGING_MODULE_TO_SERVICE_JSP_NAME =
            "engagingtoaservice.jsp";
    private static final String ENGAGING_MODULE_TO_SERVICE_GROUP_JSP_NAME =
            "EngageToServiceGroup.jsp";
    private static final String ENGAGING_MODULE_GLOBALLY_JSP_NAME = "engagingglobally.jsp";
    public static final String ADMIN_JSP_NAME = "admin.jsp";
    private static final String VIEW_GLOBAL_HANDLERS_JSP_NAME = "ViewGlobalHandlers.jsp";
    private static final String VIEW_SERVICE_HANDLERS_JSP_NAME = "ViewServiceHandlers.jsp";
    private static final String SERVICE_PARA_EDIT_JSP_NAME = "ServiceParaEdit.jsp";
    private static final String ENGAGE_TO_OPERATION_JSP_NAME = "engagingtoanoperation.jsp";

    /**
     * Field allowListServices
     */

    /**
     * Field allowListSingleService
     */
    private OutputStream out = null;
    private ConfigurationContext configContext;
    public static final String RUNNING_PORT = "RUNNING_PORT";

    public ListingAgent(ConfigurationContext configContext) {
        this.configContext = configContext;
    }

    private void adminLogging(HttpServletRequest req, HttpServletResponse res)
            throws AdminAppException, IOException {
        String username = req.getParameter("userName");
        String password = req.getParameter("password");

        if ((username == null) || (password == null) || username.trim().equals("")
                || password.trim().equals("")) {
            throw new AdminAppException(Messages.getMessage("invaliduser"));
        }

        String adminUserName = (String) configContext.getAxisConfiguration().getParameter(
                Constants.USER_NAME).getValue();
        String adminPassword = (String) configContext.getAxisConfiguration().getParameter(
                Constants.PASSWORD).getValue();

        if (username.equals(adminUserName) && password.equals(adminPassword)) {
            req.getSession().setAttribute(Constants.LOGGED, "Yes");
            res.sendRedirect(ADMIN_JSP_NAME);
        } else {
            throw new AdminAppException(Messages.getMessage("invaliduser"));
        }
    }

    private void changeParameters(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        if (req.getParameter("editServicepara") != null) {
            String serviceName = req.getParameter("axisService");
            AxisService service = configContext.getAxisConfiguration().getService(serviceName);

            if (service != null) {
                ArrayList service_para = service.getParameters();

                for (int i = 0; i < service_para.size(); i++) {
                    Parameter parameter = (Parameter) service_para.get(i);
                    String para = req.getParameter(serviceName + "_" + parameter.getName());

                    service.addParameter(new Parameter(parameter.getName(), para));
                }

                for (Iterator iterator = service.getOperations(); iterator.hasNext();) {
                    AxisOperation axisOperation = (AxisOperation) iterator.next();
                    String op_name = axisOperation.getName().getLocalPart();
                    ArrayList operation_para = axisOperation.getParameters();

                    for (int i = 0; i < operation_para.size(); i++) {
                        Parameter parameter = (Parameter) operation_para.get(i);
                        String para = req.getParameter(op_name + "_" + parameter.getName());

                        axisOperation.addParameter(new Parameter(parameter.getName(), para));
                    }
                }
            }

            res.setContentType("text/css");

            PrintWriter out_writer = new PrintWriter(out);

            out_writer.println("Parameters  changed Successfully");
            out_writer.flush();
            out_writer.close();
            req.getSession().removeAttribute(Constants.SERVICE);

            return;
        } else {
            String service = req.getParameter("axisService");

            if (service != null) {
                req.getSession().setAttribute(
                        Constants.SERVICE, configContext.getAxisConfiguration().getService(service));
            }
        }

        res.sendRedirect(SERVICE_PARA_EDIT_JSP_NAME);
    }

    private void engageModulesGlobally(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);

        String moduleName = req.getParameter("modules");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);

        if (moduleName != null) {
            try {
                configContext.getAxisConfiguration().engageModule(new QName(moduleName));
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                        moduleName + " module engaged globally Successfully");
            } catch (AxisFault axisFault) {
                req.getSession().setAttribute(Constants.ENGAGE_STATUS, axisFault.getMessage());
            }
        }

        req.getSession().setAttribute("modules", null);
        res.sendRedirect(ENGAGING_MODULE_GLOBALLY_JSP_NAME);
    }

    private void engageModulesToOperation(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);

        String moduleName = req.getParameter("modules");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        req.getSession().setAttribute("modules", null);

        String serviceName = req.getParameter("axisService");

        if (serviceName != null) {
            req.getSession().setAttribute("service", serviceName);
        } else {
            serviceName = (String) req.getSession().getAttribute("service");
        }

        req.getSession().setAttribute(
                Constants.OPERATION_MAP,
                configContext.getAxisConfiguration().getService(serviceName).getOperations());
        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);

        String operationName = req.getParameter("axisOperation");

        if ((serviceName != null) && (moduleName != null) && (operationName != null)) {
            try {
                AxisOperation od = configContext.getAxisConfiguration().getService(
                        serviceName).getOperation(new QName(operationName));

                od.engageModule(
                        configContext.getAxisConfiguration().getModule(new QName(moduleName)),
                        configContext.getAxisConfiguration());
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                        moduleName
                                + " module engaged to the operation Successfully");
            } catch (AxisFault axisFault) {
                req.getSession().setAttribute(Constants.ENGAGE_STATUS, axisFault.getMessage());
            }
        }

        req.getSession().setAttribute("operation", null);
        res.sendRedirect(ENGAGE_TO_OPERATION_JSP_NAME);
    }

    private void engageModulesToService(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);

        HashMap services = configContext.getAxisConfiguration().getServices();

        req.getSession().setAttribute(Constants.SERVICE_MAP, services);

        String moduleName = req.getParameter("modules");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        req.getSession().setAttribute("modules", null);

        String serviceName = req.getParameter("service");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);

        if ((serviceName != null) && (moduleName != null)) {
            try {
                configContext.getAxisConfiguration().getService(serviceName).engageModule(
                        configContext.getAxisConfiguration().getModule(new QName(moduleName)),
                        configContext.getAxisConfiguration());
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                        moduleName
                                + " module engaged to the service Successfully");
            } catch (AxisFault axisFault) {
                req.getSession().setAttribute(Constants.ENGAGE_STATUS, axisFault.getMessage());
            }
        }

        req.getSession().setAttribute("service", null);
        res.sendRedirect(ENGAGING_MODULE_TO_SERVICE_JSP_NAME);
    }

    private void engageModulesToServiceGroup(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);

        Iterator services = configContext.getAxisConfiguration().getServiceGroups();

        req.getSession().setAttribute(Constants.SERVICE_GROUP_MAP, services);

        String moduleName = req.getParameter("modules");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        req.getSession().setAttribute("modules", null);

        String serviceName = req.getParameter("service");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);

        if ((serviceName != null) && (moduleName != null)) {
            configContext.getAxisConfiguration().getServiceGroup(serviceName).engageModule(
                    configContext.getAxisConfiguration().getModule(new QName(moduleName)));
            req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                    moduleName
                            + " module engaged to the serviceGroup Successfully");
        }

        req.getSession().setAttribute("service", null);
        res.sendRedirect(ENGAGING_MODULE_TO_SERVICE_GROUP_JSP_NAME);
    }

    public void handle(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse, OutputStream out)
            throws IOException, Exception {
        this.out = out;

        String filePart = httpServletRequest.getRequestURL().toString();

        if ((filePart != null) && filePart.endsWith(Constants.ADMIN_LISTSERVICES)) {
            listAdminServices(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.LIST_MODULES)) {
            listModules(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null)
                && filePart.endsWith(Constants.LIST_GLOABLLY_ENGAGED_MODULES)) {
            listGloballyModules(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.LIST_PHASES)) {
            listPhases(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.ENGAGE_GLOBAL_MODULE)) {
            engageModulesGlobally(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.ENGAGE_MODULE_TO_SERVICE)) {
            engageModulesToService(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null)
                && filePart.endsWith(Constants.ENGAGE_MODULE_TO_SERVICE_GROUP)) {
            engageModulesToServiceGroup(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.ADMIN_LOGIN)) {
            adminLogging(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.VIEW_GLOBAL_HANDLERS)) {
            viewGlobalHandlers(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.SELECT_SERVICE)) {
            selectService(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.VIEW_SERVICE_HANDLERS)) {
            viewServiceHandlers(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null)
                && filePart.endsWith(Constants.LIST_SERVICE_FOR_MODULE_ENGAGEMENT)) {
            lsitServiceformodules(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null)
                && filePart.endsWith(Constants.LIST_OPERATIONS_FOR_THE_SERVICE)) {
            engageModulesToOperation(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.IN_ACTIVATE_SERVICE)) {
            inActivateService(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.ACTIVATE_SERVICE)) {
            activateService(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null)
                && filePart.endsWith(Constants.SELECT_SERVICE_FOR_PARA_EDIT)) {
            lsitServiceforParameterChanged(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.EDIR_SERVICE_PARA)) {
            changeParameters(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.LIST_SERVICE_GROUPS)) {
            listServiceGroups(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.LIST_CONTEXTS)) {
            listContexts(httpServletRequest, httpServletResponse);

            return;
        } else if ((filePart != null) && filePart.endsWith(Constants.LOGOUT)) {
            logout(httpServletRequest, httpServletResponse);
            return;
        }

        if ((filePart != null) && filePart.endsWith(Constants.LIST_SERVICES)) {
            listServices(httpServletRequest, httpServletResponse);
        } else {
            listService(httpServletRequest, httpServletResponse, filePart);
        }
    }

    /**
     * @param req
     * @param res
     * @throws IOException
     */
    private void listAdminServices(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap services = configContext.getAxisConfiguration().getServices();

        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.ERROR_SERVICE_MAP,
                configContext.getAxisConfiguration().getFaultyServices());
        res.sendRedirect(LIST_SERVICES_JSP_NAME);
    }

    private void listContexts(HttpServletRequest req, HttpServletResponse res) throws IOException {
        req.getSession().setAttribute(Constants.CONFIG_CONTEXT, configContext);
        res.sendRedirect("ViewContexts.jsp");
    }

    private void listGloballyModules(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        Collection modules = configContext.getAxisConfiguration().getEngagedModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        res.sendRedirect(LIST_GLOABLLY_ENGAGED_MODULES_JSP_NAME);
    }

    private void listModules(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HashMap modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        req.getSession().setAttribute(Constants.ERROR_MODULE_MAP,
                configContext.getAxisConfiguration().getFaultyModules());
        res.sendRedirect(LIST_AVAILABLE_MODULES_JSP_NAME);
    }

    private void listPhases(HttpServletRequest req, HttpServletResponse res) throws IOException {
        ArrayList phaselist = new ArrayList();
        PhasesInfo info = configContext.getAxisConfiguration().getPhasesInfo();

        phaselist.add(info.getINPhases());
        phaselist.add(info.getIN_FaultPhases());
        phaselist.add(info.getOUTPhases());
        phaselist.add(info.getOUT_FaultPhases());
        phaselist.add(info.getOperationInPhases());
        phaselist.add(info.getOperationInFaultPhases());
        phaselist.add(info.getOperationOutPhases());
        phaselist.add(info.getOperationOutFaultPhases());
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
    private void listService(HttpServletRequest req, HttpServletResponse res, String filePart)
            throws IOException {
        String serviceName = filePart.substring(filePart.lastIndexOf("/") + 1, filePart.length());
        HashMap services = configContext.getAxisConfiguration().getServices();
        String wsdl = req.getParameter("wsdl");
        String xsd = req.getParameter("xsd");
        if ((services != null) && !services.isEmpty()) {
            Object serviceObj = services.get(serviceName);
            if (serviceObj != null) {
                if (wsdl != null) {
                    res.setContentType("text/xml");
                    int ipindex = filePart.indexOf("//");
                    String ip = null;
                    if (ipindex >= 0) {
                        ip = filePart.substring(ipindex + 2, filePart.length());
                        int seperatorIndex = ip.indexOf(":");
                        int slashIndex = ip.indexOf("/");
                        String port = ip.substring(seperatorIndex + 1, slashIndex);
                        System.setProperty(RUNNING_PORT, port);
                        if (seperatorIndex > 0) {
                            ip = ip.substring(0, seperatorIndex);
                        }
                    }
                    ((AxisService) serviceObj).printWSDL(out, ip);
                    out.flush();
                    out.close();
                    return;
                } else if (xsd != null) {
                    res.setContentType("text/xml");
                    ((AxisService) serviceObj).printSchema(out);
                    out.flush();
                    out.close();
                    return;
                } else {
                    req.getSession().setAttribute(Constants.SINGLE_SERVICE, serviceObj);
                }
            } else {
                req.getSession().setAttribute(Constants.SINGLE_SERVICE, null);
            }
        }
        String URI = req.getRequestURI();
        URI = URI.substring(0, URI.indexOf("services"));
        res.sendRedirect(URI + LIST_SINGLE_SERVICE_JSP_NAME);
    }

    private void listServiceGroups(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        Iterator serviceGroups = configContext.getAxisConfiguration().getServiceGroups();
        HashMap services = configContext.getAxisConfiguration().getServices();

        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.SERVICE_GROUP_MAP, serviceGroups);
        res.sendRedirect(LIST_SERVICE_GROUP_JSP);
    }

    /**
     * Method listServices
     *
     * @param req
     * @param res
     * @throws IOException
     */
    private void listServices(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HashMap services = configContext.getAxisConfiguration().getServices();

        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.ERROR_SERVICE_MAP,
                configContext.getAxisConfiguration().getFaultyServices());
        res.sendRedirect(LIST_MULTIPLE_SERVICE_JSP_NAME);
    }

    private void logout(HttpServletRequest req, HttpServletResponse res) throws IOException {
        req.getSession().invalidate();
        res.sendRedirect("index.jsp");
    }

    private void lsitServiceforParameterChanged(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap services = configContext.getAxisConfiguration().getServices();

        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "SERVICE_PARAMETER");
        res.sendRedirect(SELECT_SERVICE_JSP_NAME);
    }

    private void lsitServiceformodules(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HashMap services = configContext.getAxisConfiguration().getServices();

        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "MODULE");
        res.sendRedirect(SELECT_SERVICE_JSP_NAME);
    }

    private void activateService(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getParameter("submit") != null) {
            String serviceName = req.getParameter("axisService");
            String turnon = req.getParameter("turnon");
            if (serviceName != null) {
                if (turnon != null) {
                    AxisService service = configContext.getAxisConfiguration().getServiceForActivation(serviceName);
                    service.setActive(true);
                }
            }
        } else {
            HashMap services = configContext.getAxisConfiguration().getServices();
            req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        }

        res.sendRedirect(ACTIVATE_SERVICE_JSP_NAME);
    }

    private void inActivateService(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getParameter("submit") != null) {
            String serviceName = req.getParameter("axisService");
            String turnoff = req.getParameter("turnoff");
            if (serviceName != null) {
                if (turnoff != null) {
                    AxisService service = configContext.getAxisConfiguration().getService(serviceName);
                    service.setActive(false);
                }
            }
        } else {
            HashMap services = configContext.getAxisConfiguration().getServices();
            req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        }

        res.sendRedirect(IN_ACTIVATE_SERVICE_JSP_NAME);
    }

    private void selectService(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HashMap services = configContext.getAxisConfiguration().getServices();

        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "VIEW");
        res.sendRedirect(SELECT_SERVICE_JSP_NAME);
    }

    private void viewGlobalHandlers(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        req.getSession().setAttribute(Constants.GLOBAL_HANDLERS,
                configContext.getAxisConfiguration());
        res.sendRedirect(VIEW_GLOBAL_HANDLERS_JSP_NAME);
    }

    private void viewServiceHandlers(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        String service = req.getParameter("axisService");

        if (service != null) {
            req.getSession().setAttribute(Constants.SERVICE_HANDLERS,
                    configContext.getAxisConfiguration().getService(service));
        }

        res.sendRedirect(VIEW_SERVICE_HANDLERS_JSP_NAME);
    }
}
