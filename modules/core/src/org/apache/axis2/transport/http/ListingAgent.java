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
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterImpl;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfigurationImpl;
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

    private static final String LIST_SRVICES_JSP_NAME = "listService.jsp";

    private static final String SELECT_SERVICE_JSP_NAME = "SelectService.jsp";

    private static final String REMOVE_SERVICE_JSP_NAME = "RemoveService.jsp";

    private static final String ADMIN_JSP_NAME = "admin.jsp";

    private static final String LIST_AVAILABLE_MODULES_JSP_NAME = "listModules.jsp";

    private static final String LIST_GLOABLLY_ENGAGED_MODULES_JSP_NAME = "globalModules.jsp";

    private static final String LIST_PHASES_JSP_NAME = "viewphases.jsp";

    private static final String ENGAGING_MODULE_GLOBALLY_JSP_NAME = "engagingglobally.jsp";

    private static final String ENGAGING_MODULE_TO_SERVICE_JSP_NAME = "engagingtoaservice.jsp";

    /**
     * Field LIST_SINGLE_SERVICE_JSP_NAME
     */
    private static final String LIST_SINGLE_SERVICE_JSP_NAME = "listSingleService.jsp";
    private static final String VIEW_GLOBAL_HANDLERS_JSP_NAME = "ViewGlobalHandlers.jsp";
    private static final String VIEW_SERVICE_HANDLERS_JSP_NAME = "ViewServiceHandlers.jsp";

    private static final String ENGAGE_TO_OPERATION_JSP_NAME = "enaggingtoanopeartion.jsp";
    private static final String SERVICE_PARA_EDIT_JSP_NAME = "ServiceParaEdit.jsp";

    public ListingAgent(ConfigurationContext configContext) {
        this.configContext = configContext;
    }

    private ConfigurationContext configContext;
    /**
     * Field allowListServices
     */
    private final boolean allowListServices = true;

    /**
     * Field allowListSingleService
     */
    private final boolean allowListSingleService = true;
    private OutputStream out = null;

    public void handle(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse,
                       OutputStream out)
            throws IOException,Exception {
        this.out = out;
        String filePart = httpServletRequest.getRequestURL().toString();
        if ((filePart != null) &&
                filePart.endsWith(Constants.ADMIN_LISTSERVICES)) {
            listAdminServices(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null) &&
                filePart.endsWith(Constants.LIST_MODULES)) {
            listModules(httpServletRequest, httpServletResponse);
            return;
        } else if (
                (filePart != null) &&
                filePart.endsWith(Constants.LIST_GLOABLLY_ENGAGED_MODULES)) {
            listGloballyModules(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null) &&
                filePart.endsWith(Constants.LIST_PHASES)) {
            listPhases(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null) &&
                filePart.endsWith(Constants.ENGAGE_GLOBAL_MODULE)) {
            engageModulesGlobally(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null) &&
                filePart.endsWith(Constants.ENGAGE_MODULE_TO_SERVICE)) {
            engageModulesToService(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null) &&
                filePart.endsWith(Constants.ADMIN_LOGGING)) {
            adminLogging(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null) &&
                filePart.endsWith(Constants.VIEW_GLOBAL_HANDLERS)) {
            viewGlobalHandlers(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null) &&
                filePart.endsWith(Constants.SELECT_SERVICE)) {
            selectService(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null) &&
                filePart.endsWith(Constants.VIEW_SERVICE_HANDLERS)) {
            viewServiceHandlers(httpServletRequest, httpServletResponse);
            return;
        } else if (
                (filePart != null) &&
                filePart.endsWith(Constants.LIST_SERVICE_FOR_MODULE_ENGAMNET)) {
            lsitServiceformodules(httpServletRequest, httpServletResponse);
            return;
        } else if (
                (filePart != null) &&
                filePart.endsWith(Constants.LIST_OPERATIONS_FOR_THE_SERVICE)) {
            engageModulesToOpeartion(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null) &&
                filePart.endsWith(Constants.REMOVE_SERVICE)) {
            removeService(httpServletRequest, httpServletResponse);
            return;
        }else if ((filePart != null) &&
                filePart.endsWith(Constants.SELECT_SERVICE_FOR_PARA_EDIT)) {
            lsitServiceforParameterChanged(httpServletRequest, httpServletResponse);
            return;
        } else if ((filePart != null) &&
                filePart.endsWith(Constants.EDIR_SERVICE_PARA)) {
            chageParameters(httpServletRequest, httpServletResponse);
            return;
        }

        if ((filePart != null) &&
                filePart.endsWith(Constants.LISTSERVICES)) {
            listServices(httpServletRequest, httpServletResponse);
            return;
        } else {
            if (allowListSingleService) {
                listService(httpServletRequest, httpServletResponse, filePart);
                return;
            }
        }
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

    /**
     * @param req
     * @param res
     * @throws IOException
     */
    private void listAdminServices(HttpServletRequest req,
                                   HttpServletResponse res)
            throws IOException {
        HashMap services = configContext.getAxisConfiguration().getServices();
        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.ERROR_SERVICE_MAP,
                configContext.getAxisConfiguration().getFaultyServices());
        res.sendRedirect(LIST_SRVICES_JSP_NAME);
    }

    private void selectService(HttpServletRequest req,
                               HttpServletResponse res)
            throws IOException {
        HashMap services = configContext.getAxisConfiguration().getServices();
        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "VIEW");
        res.sendRedirect(SELECT_SERVICE_JSP_NAME);
    }

    private void adminLogging(HttpServletRequest req, HttpServletResponse res) throws AdminAppException, IOException {
        String username = req.getParameter("userName");
        String password = req.getParameter("password");
        if (username == null
                || password == null
                || username.trim().equals("")
                || password.trim().equals("")) {
            throw new AdminAppException(Messages.getMessage("invaliduser"));
        }
        String adminUserName =
                (String) configContext.getAxisConfiguration()
                .getParameter(Constants.USER_NAME)
                .getValue();
        String adminPassword =
                (String) configContext.getAxisConfiguration()
                .getParameter(Constants.PASSWORD)
                .getValue();
        if (username != null
                && password != null
                && username.equals(adminUserName)
                && password.equals(adminPassword)) {
            req.getSession().setAttribute(Constants.LOGGED, "Yes");
            res.sendRedirect(ADMIN_JSP_NAME);
        } else {
//            res.setContentType("text/css");
//            PrintWriter out_writer = new PrintWriter(out);
//            out_writer.println("Invalid user name password");
//            out_writer.flush();
//            out_writer.close();
            throw new AdminAppException(Messages.getMessage("invaliduser"));
        }
    }

    private void listModules(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HashMap modules =
                ((AxisConfigurationImpl) configContext.getAxisConfiguration()).getModules();
        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        req.getSession().setAttribute(Constants.ERROR_MODULE_MAP,
                configContext.getAxisConfiguration().getFaultyModules());
        res.sendRedirect(LIST_AVAILABLE_MODULES_JSP_NAME);
    }

    private void engageModulesGlobally(HttpServletRequest req,
                                       HttpServletResponse res)
            throws IOException {
        HashMap modules =
                ((AxisConfigurationImpl) configContext.getAxisConfiguration()).getModules();
        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        String moduleName = req.getParameter("modules");
        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        if (moduleName != null) {
            try {
                configContext.getAxisConfiguration().engageModule(
                        new QName(moduleName));
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                        moduleName + " module engaged globally Successfully");
            } catch (AxisFault axisFault) {
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                        axisFault.getMessage());
            }
        }
        req.getSession().setAttribute("modules", null);
        res.sendRedirect(ENGAGING_MODULE_GLOBALLY_JSP_NAME);
    }

    private void engageModulesToOpeartion(HttpServletRequest req,
                                          HttpServletResponse res)
            throws IOException {
        HashMap modules =
                ((AxisConfigurationImpl) configContext.getAxisConfiguration()).getModules();
        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        String moduleName = req.getParameter("modules");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        req.getSession().setAttribute("modules", null);

        String serviceName = req.getParameter("service");
        if (serviceName != null) {
            req.getSession().setAttribute("service", serviceName);
        } else {
            serviceName = (String) req.getSession().getAttribute("service");
        }
        req.getSession().setAttribute(Constants.OPEARTION_MAP,
                configContext
                .getAxisConfiguration()
                .getService(serviceName)
                .getOperations());
        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        String operationName = req.getParameter("operation");
        if (serviceName != null && moduleName != null && operationName != null) {
            try {
                OperationDescription od =
                        configContext.getAxisConfiguration().getService(
                                serviceName)
                        .getOperation(new QName(operationName));
                od.engageModule(
                        configContext.getAxisConfiguration().getModule(
                                new QName(moduleName)));
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                        moduleName +
                        " module engaged to the operation Successfully");
            } catch (AxisFault axisFault) {
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                        axisFault.getMessage());
            }
        }
        req.getSession().setAttribute("operation", null);
        res.sendRedirect(ENGAGE_TO_OPERATION_JSP_NAME);
    }

    private void engageModulesToService(HttpServletRequest req,
                                        HttpServletResponse res)
            throws IOException {
        HashMap modules =
                ((AxisConfigurationImpl) configContext.getAxisConfiguration()).getModules();
        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        HashMap services = configContext.getAxisConfiguration().getServices();
        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        String moduleName = req.getParameter("modules");
        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        req.getSession().setAttribute("modules", null);
        String serviceName = req.getParameter("service");
        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        if (serviceName != null && moduleName != null) {
            try {

                configContext.getAxisConfiguration().getService(
                        serviceName)
                        .engageModule(
                                configContext.getAxisConfiguration().getModule(
                                        new QName(moduleName)),configContext.getAxisConfiguration());
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                        moduleName +
                        " module engaged to the service Successfully");
            } catch (AxisFault axisFault) {
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                        axisFault.getMessage());
            }
        }
        req.getSession().setAttribute("service", null);
        res.sendRedirect(ENGAGING_MODULE_TO_SERVICE_JSP_NAME);
    }

    private void listGloballyModules(HttpServletRequest req,
                                     HttpServletResponse res)
            throws IOException {
        Collection modules =
                ((AxisConfigurationImpl) configContext.getAxisConfiguration()).getEngadgedModules();
        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        res.sendRedirect(LIST_GLOABLLY_ENGAGED_MODULES_JSP_NAME);
    }

    private void lsitServiceformodules(HttpServletRequest req,
                                       HttpServletResponse res)
            throws IOException {
        HashMap services = configContext.getAxisConfiguration().getServices();
        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "MODULE");
        res.sendRedirect(SELECT_SERVICE_JSP_NAME);
    }

    private void lsitServiceforParameterChanged(HttpServletRequest req,
                                                HttpServletResponse res)
            throws IOException {
        HashMap services = configContext.getAxisConfiguration().getServices();
        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "SERVICE_PARAMETER");
        res.sendRedirect(SELECT_SERVICE_JSP_NAME);
    }



    private void chageParameters(HttpServletRequest req,
                                 HttpServletResponse res)
            throws IOException {
        if(req.getParameter("changePara")!=null){
            String serviceName = req.getParameter("service");
            ServiceDescription service =  configContext.getAxisConfiguration().
                    getService(serviceName);
            if(service !=null){
                ArrayList service_para = service.getParameters();
                for (int i = 0; i < service_para.size(); i++) {
                    Parameter parameter = (Parameter) service_para.get(i);
                    String para =  req.getParameter(serviceName + "_" + parameter.getName());
                    service.addParameter(new ParameterImpl(parameter.getName(),para));
                }
                HashMap operation = service.getOperations();
                Collection op_col = operation.values();
                for (Iterator iterator = op_col.iterator(); iterator.hasNext();) {
                    OperationDescription operationDescription =
                            (OperationDescription) iterator.next();
                    String op_name = operationDescription.getName().getLocalPart();
                    ArrayList operation_para = operationDescription.getParameters();
                    for (int i = 0; i < operation_para.size(); i++) {
                        Parameter parameter = (Parameter) operation_para.get(i);
                        String para =  req.getParameter(op_name + "_" + parameter.getName());
                        operationDescription.addParameter(
                                new ParameterImpl(parameter.getName(),para));
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

        }   else {
            String service = req.getParameter("service");
            if (service != null) {
                req.getSession().setAttribute(Constants.SERVICE,
                        configContext.getAxisConfiguration().getService(
                                service));
            }
        }
        res.sendRedirect(SERVICE_PARA_EDIT_JSP_NAME);
    }

    private void removeService(HttpServletRequest req,
                               HttpServletResponse res)
            throws IOException {
        if (req.getParameter("submit") != null) {
            String serviceName = req.getParameter("service");
            String turnoff = req.getParameter("turnoff") ;
//            String remove = req.getParameter("remove");
            if(serviceName !=null){
//                if (remove !=null){
//                    ServiceDescription service =  configContext.getAxisConfiguration().
//                            getService(new QName(serviceName));
//                    if(service != null ){
//                        File servicefile = new File(service.getFileName());
//                        if(servicefile.exists()){
//                            configContext.getAxisConfiguration().removeService(
//                                    new QName(serviceName));
//                            servicefile.delete();
//                        }
//
//                    }
//                }
                if(turnoff !=null){
                    configContext.getAxisConfiguration().removeService(serviceName);
                    res.setContentType("text/css");
                    PrintWriter out_writer = new PrintWriter(out);
                    out_writer.println("Service removed from the system Successfully");
                    out_writer.flush();
                    out_writer.close();
                    return;
                }
            }

        } else {
            HashMap services = configContext.getAxisConfiguration().getServices();
            req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        }
        res.sendRedirect(REMOVE_SERVICE_JSP_NAME);
    }

    private void viewGlobalHandlers(HttpServletRequest req,
                                    HttpServletResponse res)
            throws IOException {
        req.getSession().setAttribute(Constants.GLOBAL_HANDLERS,
                configContext.getAxisConfiguration());
        res.sendRedirect(VIEW_GLOBAL_HANDLERS_JSP_NAME);
    }

    private void viewServiceHandlers(HttpServletRequest req,
                                     HttpServletResponse res)
            throws IOException {
        String service = req.getParameter("service");
        if (service != null) {
            req.getSession().setAttribute(Constants.SERVICE_HANDLERS,
                    configContext.getAxisConfiguration().getService(
                            service));
        }
        res.sendRedirect(VIEW_SERVICE_HANDLERS_JSP_NAME);
    }

    private void listPhases(HttpServletRequest req, HttpServletResponse res) throws IOException {
        ArrayList phaselist = new ArrayList();
        PhasesInfo info = ((AxisConfigurationImpl) configContext.getAxisConfiguration()).getPhasesinfo();
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
    private void listService(HttpServletRequest req,
                             HttpServletResponse res,
                             String filePart)
            throws IOException {
        String serviceName = filePart.substring(filePart.lastIndexOf("/") + 1,
                filePart.length());
        HashMap services = configContext.getAxisConfiguration().getServices();
        String wsdl = req.getParameter("wsdl");
        if ((services != null) && !services.isEmpty()) {
            Object serviceObj = services.get(serviceName);
            if (serviceObj != null) {
                if (wsdl != null) {
                    res.setContentType("text/html");
                    PrintWriter out_writer = new PrintWriter(out);
                    ((ServiceDescription) serviceObj).printWSDL(out_writer,
                            filePart);
                    out.flush();
                    out.close();
                    wsdl = null;
                    return;
                } else {
                    req.getSession().setAttribute(Constants.SINGLE_SERVICE,
                            serviceObj);
                }
            }
        }
        String URI = req.getRequestURI();
        URI = URI.substring(0, URI.indexOf("services"));
        res.sendRedirect(URI + LIST_SINGLE_SERVICE_JSP_NAME);
    }

}
