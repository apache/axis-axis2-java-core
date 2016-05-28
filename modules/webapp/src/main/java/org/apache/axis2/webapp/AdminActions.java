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

package org.apache.axis2.webapp;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.AbstractAgent;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides methods to process axis2 admin requests.
 */
final class AdminActions {
    private static final Log log = LogFactory.getLog(AbstractAgent.class);
    /**
     * Field LIST_MULTIPLE_SERVICE_JSP_NAME
     */
    private static final String LIST_SERVICE_GROUP_JSP = "ListServiceGroup.jsp";
    private static final String LIST_SERVICES_JSP_NAME = "listService.jsp";
    private static final String LIST_SINGLE_SERVICES_JSP_NAME = "listSingleService.jsp";
    private static final String SELECT_SERVICE_JSP_NAME = "SelectService.jsp";
    private static final String IN_ACTIVATE_SERVICE_JSP_NAME = "InActivateService.jsp";
    private static final String ACTIVATE_SERVICE_JSP_NAME = "ActivateService.jsp";

    /**
     * Field LIST_SINGLE_SERVICE_JSP_NAME
     */
    private static final String LIST_PHASES_JSP_NAME = "viewphases.jsp";
    private static final String LIST_GLOABLLY_ENGAGED_MODULES_JSP_NAME = "globalModules.jsp";
    private static final String LIST_AVAILABLE_MODULES_JSP_NAME = "listModules.jsp";
    private static final String ENGAGING_MODULE_TO_SERVICE_JSP_NAME = "engagingtoaservice.jsp";
    private static final String ENGAGING_MODULE_TO_SERVICE_GROUP_JSP_NAME =
            "EngageToServiceGroup.jsp";
    private static final String ENGAGING_MODULE_GLOBALLY_JSP_NAME = "engagingglobally.jsp";
    public static final String ADMIN_JSP_NAME = "admin.jsp";
    private static final String VIEW_GLOBAL_HANDLERS_JSP_NAME = "ViewGlobalHandlers.jsp";
    private static final String VIEW_SERVICE_HANDLERS_JSP_NAME = "ViewServiceHandlers.jsp";
    private static final String SERVICE_PARA_EDIT_JSP_NAME = "ServiceParaEdit.jsp";
    private static final String ENGAGE_TO_OPERATION_JSP_NAME = "engagingtoanoperation.jsp";
    private static final String LOGIN_JSP_NAME = "Login.jsp";

    private final ConfigurationContext configContext;
    private File serviceDir;

    public AdminActions(ConfigurationContext configContext) {
        this.configContext = configContext;
        try {
            if (configContext.getAxisConfiguration().getRepository() != null) {
                File repoDir =
                        new File(configContext.getAxisConfiguration().getRepository().getFile());
                serviceDir = new File(repoDir, "services");
                if (!serviceDir.exists()) {
                    serviceDir.mkdirs();
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void populateSessionInformation(HttpServletRequest req) {
        HashMap services = configContext.getAxisConfiguration().getServices();
        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.SERVICE_PATH, configContext.getServicePath());
    }

    @Action(name="index")
    public View processIndex(HttpServletRequest req) {
        return new View(ADMIN_JSP_NAME);
    }

    // supported web operations

    @Action(name="welcome", authorizationRequired=false)
    public View processWelcome(HttpServletRequest req) {
        return new View(LOGIN_JSP_NAME);
    }

    @Action(name="upload")
    public View processUpload(HttpServletRequest req) {
        String hasHotDeployment =
                (String) configContext.getAxisConfiguration().getParameterValue("hotdeployment");
        String hasHotUpdate =
                (String) configContext.getAxisConfiguration().getParameterValue("hotupdate");
        req.setAttribute("hotDeployment", (hasHotDeployment.equals("true")) ? "enabled"
                : "disabled");
        req.setAttribute("hotUpdate", (hasHotUpdate.equals("true")) ? "enabled" : "disabled");
        RequestContext reqContext = new ServletRequestContext(req);

        boolean isMultipart = ServletFileUpload.isMultipartContent(reqContext);
        if (isMultipart) {

            try {
                //Create a factory for disk-based file items
                FileItemFactory factory = new DiskFileItemFactory();
                //Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<?> items = upload.parseRequest(req);
                // Process the uploaded items
                Iterator<?> iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();
                    if (!item.isFormField()) {

                        String fileName = item.getName();
                        String fileExtesion = fileName;
                        fileExtesion = fileExtesion.toLowerCase();
                        if (!(fileExtesion.endsWith(".jar") || fileExtesion.endsWith(".aar"))) {
                            req.setAttribute("status", "failure");
                            req.setAttribute("cause", "Unsupported file type " + fileExtesion);
                        } else {

                            String fileNameOnly;
                            if (fileName.indexOf("\\") < 0) {
                                fileNameOnly =
                                        fileName.substring(fileName.lastIndexOf("/") + 1, fileName
                                                .length());
                            } else {
                                fileNameOnly =
                                        fileName.substring(fileName.lastIndexOf("\\") + 1, fileName
                                                .length());
                            }

                            File uploadedFile = new File(serviceDir, fileNameOnly);
                            item.write(uploadedFile);
                            req.setAttribute("status", "success");
                            req.setAttribute("filename", fileNameOnly);
                        }
                    }
                }
            } catch (Exception e) {
                req.setAttribute("status", "failure");
                req.setAttribute("cause", e.getMessage());

            }
        }
        return new View("upload.jsp");
    }

    @Action(name="login", authorizationRequired=false)
    public ActionResult processLogin(HttpServletRequest req) {
        String username = req.getParameter("userName");
        String password = req.getParameter("password");

        if ((username == null) || (password == null) || username.trim().length() == 0
                || password.trim().length() == 0) {
            req.setAttribute("errorMessage", "Invalid auth credentials!");
            return new View(LOGIN_JSP_NAME);
        }

        String adminUserName = (String) configContext.getAxisConfiguration().getParameter(
                Constants.USER_NAME).getValue();
        String adminPassword = (String) configContext.getAxisConfiguration().getParameter(
                Constants.PASSWORD).getValue();

        if (username.equals(adminUserName) && password.equals(adminPassword)) {
            req.getSession().setAttribute(Constants.LOGGED, "Yes");
            return new Redirect("index");
        } else {
            req.setAttribute("errorMessage", "Invalid auth credentials!");
            return new View(LOGIN_JSP_NAME);
        }
    }

    @Action(name="editServicePara")
    public View processEditServicePara(HttpServletRequest req) throws AxisFault {
        String serviceName = req.getParameter("axisService");
        if (req.getParameter("changePara") != null) {
            AxisService service = configContext.getAxisConfiguration().getService(serviceName);
            if (service != null) {
                for (Parameter parameter : service.getParameters()) {
                    String para = req.getParameter(serviceName + "_" + parameter.getName());
                    service.addParameter(new Parameter(parameter.getName(), para));
                }

                for (Iterator<AxisOperation> iterator = service.getOperations(); iterator.hasNext();) {
                    AxisOperation axisOperation = iterator.next();
                    String op_name = axisOperation.getName().getLocalPart();

                    for (Parameter parameter : axisOperation.getParameters()) {
                        String para = req.getParameter(op_name + "_" + parameter.getName());

                        axisOperation.addParameter(new Parameter(parameter.getName(), para));
                    }
                }
            }
            req.setAttribute("status", "Parameters Changed Successfully.");
            req.getSession().removeAttribute(Constants.SERVICE);
        } else {
            AxisService serviceTemp =
                    configContext.getAxisConfiguration().getServiceForActivation(serviceName);
            if (serviceTemp.isActive()) {

                if (serviceName != null) {
                    req.getSession().setAttribute(Constants.SERVICE,
                                                  configContext.getAxisConfiguration().getService(
                                                          serviceName));
                }
            } else {
                req.setAttribute("status", "Service " + serviceName + " is not an active service" +
                        ". \n Only parameters of active services can be edited.");
            }
        }
        return new View(SERVICE_PARA_EDIT_JSP_NAME);
    }

    @Action(name="engagingGlobally")
    public View processEngagingGlobally(HttpServletRequest req) {
        Map<String,AxisModule> modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);

        String moduleName = req.getParameter("modules");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);

        if (moduleName != null) {
            try {
                configContext.getAxisConfiguration().engageModule(moduleName);
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                                              moduleName + " module engaged globally successfully");
            } catch (AxisFault axisFault) {
                req.getSession().setAttribute(Constants.ENGAGE_STATUS, axisFault.getMessage());
            }
        }

        req.getSession().setAttribute("modules", null);
        return new View(ENGAGING_MODULE_GLOBALLY_JSP_NAME);
    }

    @Action(name="listOperations")
    public View processListOperations(HttpServletRequest req) throws AxisFault {
        Map<String,AxisModule> modules = configContext.getAxisConfiguration().getModules();

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
                        configContext.getAxisConfiguration().getModule(moduleName));
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                                              moduleName
                                                      +
                                                      " module engaged to the operation successfully");
            } catch (AxisFault axisFault) {
                req.getSession().setAttribute(Constants.ENGAGE_STATUS, axisFault.getMessage());
            }
        }

        req.getSession().setAttribute("operation", null);
        return new View(ENGAGE_TO_OPERATION_JSP_NAME);
    }

    @Action(name="engageToService")
    public View processEngageToService(HttpServletRequest req) {
        Map<String,AxisModule> modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        populateSessionInformation(req);

        String moduleName = req.getParameter("modules");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        req.getSession().setAttribute("modules", null);

        String serviceName = req.getParameter("axisService");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);

        if ((serviceName != null) && (moduleName != null)) {
            try {
                configContext.getAxisConfiguration().getService(serviceName).engageModule(
                        configContext.getAxisConfiguration().getModule(moduleName));
                req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                                              moduleName
                                                      +
                                                      " module engaged to the service successfully");
            } catch (AxisFault axisFault) {
                req.getSession().setAttribute(Constants.ENGAGE_STATUS, axisFault.getMessage());
            }
        }

        req.getSession().setAttribute("axisService", null);
        return new View(ENGAGING_MODULE_TO_SERVICE_JSP_NAME);
    }

    @Action(name="engageToServiceGroup")
    public View processEngageToServiceGroup(HttpServletRequest req) throws AxisFault {
        Map<String,AxisModule> modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);

        Iterator<AxisServiceGroup> services = configContext.getAxisConfiguration().getServiceGroups();

        req.getSession().setAttribute(Constants.SERVICE_GROUP_MAP, services);

        String moduleName = req.getParameter("modules");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        req.getSession().setAttribute("modules", null);

        String serviceName = req.getParameter("axisService");

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);

        if ((serviceName != null) && (moduleName != null)) {
            configContext.getAxisConfiguration().getServiceGroup(serviceName).engageModule(
                    configContext.getAxisConfiguration().getModule(moduleName));
            req.getSession().setAttribute(Constants.ENGAGE_STATUS,
                                          moduleName
                                                  +
                                                  " module engaged to the service group successfully");
        }

        req.getSession().setAttribute("axisService", null);
        return new View(ENGAGING_MODULE_TO_SERVICE_GROUP_JSP_NAME);
    }

    @Action(name="logout")
    public View processLogout(HttpServletRequest req) {
        req.getSession().invalidate();
        return new View("index.jsp");
    }

    @Action(name="viewServiceGroupConetxt")
    public View processviewServiceGroupConetxt(HttpServletRequest req) {
        String type = req.getParameter("TYPE");
        String sgID = req.getParameter("ID");
        ServiceGroupContext sgContext = configContext.getServiceGroupContext(sgID);
        req.getSession().setAttribute("ServiceGroupContext",sgContext);
        req.getSession().setAttribute("TYPE",type);
        req.getSession().setAttribute("ConfigurationContext",configContext);
        return new View("viewServiceGroupContext.jsp");
    }

    @Action(name="viewServiceContext")
    public View processviewServiceContext(HttpServletRequest req) throws AxisFault {
        String type = req.getParameter("TYPE");
        String sgID = req.getParameter("PID");
        String ID = req.getParameter("ID");
        ServiceGroupContext sgContext = configContext.getServiceGroupContext(sgID);
        if (sgContext != null) {
            AxisService service = sgContext.getDescription().getService(ID);
            ServiceContext serviceContext = sgContext.getServiceContext(service);
            req.setAttribute("ServiceContext",serviceContext);
            req.setAttribute("TYPE",type);
        } else {
            req.setAttribute("ServiceContext",null);
            req.setAttribute("TYPE",type);
        }
        return new View("viewServiceContext.jsp");
    }

    @Action(name="selectServiceParaEdit")
    public View processSelectServiceParaEdit(HttpServletRequest req) {
        populateSessionInformation(req);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "SERVICE_PARAMETER");
        return new View(SELECT_SERVICE_JSP_NAME);
    }

    @Action(name="listOperation")
    public View processListOperation(HttpServletRequest req) {
        populateSessionInformation(req);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "MODULE");
        return new View(SELECT_SERVICE_JSP_NAME);
    }

    @Action(name="activateService")
    public View processActivateService(HttpServletRequest req) throws AxisFault {
        if (req.getParameter("submit") != null) {
            String serviceName = req.getParameter("axisService");
            String turnon = req.getParameter("turnon");
            if (serviceName != null) {
                if (turnon != null) {
                    configContext.getAxisConfiguration().startService(serviceName);
                }
            }
        }
        populateSessionInformation(req);
        return new View(ACTIVATE_SERVICE_JSP_NAME);
    }

    @Action(name="deactivateService")
    public View processDeactivateService(HttpServletRequest req) throws AxisFault {
        if (req.getParameter("submit") != null) {
            String serviceName = req.getParameter("axisService");
            String turnoff = req.getParameter("turnoff");
            if (serviceName != null) {
                if (turnoff != null) {
                    configContext.getAxisConfiguration().stopService(serviceName);
                }
                populateSessionInformation(req);
            }
        } else {
            populateSessionInformation(req);
        }

        return new View(IN_ACTIVATE_SERVICE_JSP_NAME);
    }

    @Action(name="viewGlobalHandlers")
    public View processViewGlobalHandlers(HttpServletRequest req) {
        req.getSession().setAttribute(Constants.GLOBAL_HANDLERS,
                                      configContext.getAxisConfiguration());

        return new View(VIEW_GLOBAL_HANDLERS_JSP_NAME);
    }

    @Action(name="viewServiceHandlers")
    public View processViewServiceHandlers(HttpServletRequest req) throws AxisFault {
        String service = req.getParameter("axisService");

        if (service != null) {
            req.getSession().setAttribute(Constants.SERVICE_HANDLERS,
                                          configContext.getAxisConfiguration().getService(service));
        }

        return new View(VIEW_SERVICE_HANDLERS_JSP_NAME);
    }

    @Action(name="listPhases")
    public View processListPhases(HttpServletRequest req) {
        PhasesInfo info = configContext.getAxisConfiguration().getPhasesInfo();
        req.getSession().setAttribute(Constants.PHASE_LIST, info);
        return new View(LIST_PHASES_JSP_NAME);
    }

    @Action(name="listServiceGroups")
    public View processListServiceGroups(HttpServletRequest req) {
        Iterator<AxisServiceGroup> serviceGroups = configContext.getAxisConfiguration().getServiceGroups();
        populateSessionInformation(req);
        req.getSession().setAttribute(Constants.SERVICE_GROUP_MAP, serviceGroups);

        return new View(LIST_SERVICE_GROUP_JSP);
    }

    @Action(name="listService")
    public View processListService(HttpServletRequest req) {
        populateSessionInformation(req);
        req.getSession().setAttribute(Constants.ERROR_SERVICE_MAP,
                                      configContext.getAxisConfiguration().getFaultyServices());

        return new View(LIST_SERVICES_JSP_NAME);
    }

    @Action(name="listSingleService")
    public View processListSingleService(HttpServletRequest req) throws AxisFault {
        req.getSession().setAttribute(Constants.IS_FAULTY, ""); //Clearing out any old values.
        String serviceName = req.getParameter("serviceName");
        if (serviceName != null) {
            AxisService service = configContext.getAxisConfiguration().getService(serviceName);
            req.getSession().setAttribute(Constants.SINGLE_SERVICE, service);
        }
        return new View(LIST_SINGLE_SERVICES_JSP_NAME);
    }

    @Action(name="listContexts")
    public View processListContexts(HttpServletRequest req) {
        req.getSession().setAttribute(Constants.CONFIG_CONTEXT, configContext);
        return new View("ViewContexts.jsp");
    }

    @Action(name="globalModules")
    public View processglobalModules(HttpServletRequest req) {
        Collection<AxisModule> modules = configContext.getAxisConfiguration().getEngagedModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);

        return new View(LIST_GLOABLLY_ENGAGED_MODULES_JSP_NAME);
    }

    @Action(name="listModules")
    public View processListModules(HttpServletRequest req) {
        Map<String,AxisModule> modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        req.getSession().setAttribute(Constants.ERROR_MODULE_MAP,
                                      configContext.getAxisConfiguration().getFaultyModules());

        return new View(LIST_AVAILABLE_MODULES_JSP_NAME);
    }

    @Action(name="disengageModule")
    public View processdisengageModule(HttpServletRequest req) throws AxisFault {
        String type = req.getParameter("type");
        String serviceName = req.getParameter("serviceName");
        String moduleName = req.getParameter("module");
        AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
        AxisService service = axisConfiguration.getService(serviceName);
        AxisModule module = axisConfiguration.getModule(moduleName);
        if (type.equals("operation")) {
            if (service.isEngaged(module.getName()) ||
                    axisConfiguration.isEngaged(module.getName())) {
                req.getSession().setAttribute("status", "Can not disengage module " + moduleName +
                        ". This module is engaged at a higher level.");
            } else {
                String opName = req.getParameter("operation");
                AxisOperation op = service.getOperation(new QName(opName));
                op.disengageModule(module);
                req.getSession()
                        .setAttribute("status", "Module " + moduleName + " was disengaged from " +
                                "operation " + opName + " in service " + serviceName + ".");
            }
        } else {
            if (axisConfiguration.isEngaged(module.getName())) {
                req.getSession()
                        .setAttribute("status", "Can not disengage module " + moduleName + ". " +
                                "This module is engaged at a higher level.");
            } else {
                service.disengageModule(axisConfiguration.getModule(moduleName));
                req.getSession()
                        .setAttribute("status", "Module " + moduleName + " was disengaged from" +
                                " service " + serviceName + ".");
            }
        }
        return new View("disengage.jsp");
    }

    @Action(name="deleteService")
    public View processdeleteService(HttpServletRequest req) throws AxisFault {
        String serviceName = req.getParameter("serviceName");
        AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
        if (axisConfiguration.getService(serviceName) != null) {
            axisConfiguration.removeService(serviceName);
            req.getSession().setAttribute("status", "Service '" + serviceName + "' has been successfully removed.");
        } else {
            req.getSession().setAttribute("status", "Failed to delete service '" + serviceName + "'. Service doesn't exist.");
        }

        return new View("deleteService.jsp");
    }

    @Action(name="selectService")
    public View processSelectService(HttpServletRequest req) {
        populateSessionInformation(req);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "VIEW");

        return new View(SELECT_SERVICE_JSP_NAME);
    }
}
