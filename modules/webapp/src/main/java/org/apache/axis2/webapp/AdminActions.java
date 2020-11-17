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
import org.apache.axis2.description.AxisDescription;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides methods to process axis2 admin requests.
 */
final class AdminActions {
    private static final Log log = LogFactory.getLog(AbstractAgent.class);
    
    private static final String WELCOME = "welcome";
    private static final String LOGOUT = "logout";
    private static final String INDEX = "index";
    private static final String UPLOAD = "upload";
    private static final String LIST_SERVICES = "listServices";
    private static final String ENGAGE_GLOBALLY = "engageGlobally";
    private static final String ENGAGE_TO_SERVICE_GROUP = "engageToServiceGroup";
    private static final String ENGAGE_TO_SERVICE = "engageToService";
    private static final String ENGAGE_TO_OPERATION = "engageToOperation";
    private static final String DEACTIVATE_SERVICE = "deactivateService";
    private static final String ACTIVATE_SERVICE = "activateService";
    private static final String EDIT_SERVICE_PARAMETERS = "editServiceParameters";
    private static final String VIEW_OPERATION_SPECIFIC_CHAINS = "viewOperationSpecificChains";
    private static final String HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS = "^[a-zA-Z0-9.\\-\\/+=@,:\\\\ ]*$";
    private static final String FILENAME_REGEX_INVALID_CHARS = "^[a-zA-Z0-9!@#$%^&{}\\[\\]()_+\\-=,.~'` ]{1,255}$";

    /**
     * Field LIST_MULTIPLE_SERVICE_JSP_NAME
     */
    private static final String SELECT_SERVICE_JSP_NAME = "SelectService.jsp";

    /**
     * Field LIST_SINGLE_SERVICE_JSP_NAME
     */
    private static final String LOGIN_JSP_NAME = "Login.jsp";

    private final ConfigurationContext configContext;
    private File serviceDir;

    public AdminActions(ConfigurationContext configContext) {
        this.configContext = configContext;
        try {
            if (configContext.getAxisConfiguration().getRepository() != null) {
                File repoDir =
                        new File(configContext.getAxisConfiguration().getRepository().toURI());
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

    protected void populateRequestAttributes(HttpServletRequest req) {
        req.setAttribute("configContext", configContext);
    }

    @Action(name=INDEX)
    public View index(HttpServletRequest req) {
        return new View("admin.jsp");
    }

    // supported web operations

    @Action(name=WELCOME, authorizationRequired=false)
    public ActionResult welcome(HttpServletRequest req) {
        // Session fixation prevention: if there is an existing session, first invalidate it.
        if (req.getSession(false) != null) {
            log.debug("welcome() found an active http session, first invalidate it, redirecting to: " + LOGOUT);
            return new Redirect(LOGOUT);
        } else {
            if ("true".equals(req.getParameter("failed"))) {
                log.error("welcome() received 'failed' param as true, redirecting to: " + LOGIN_JSP_NAME);
                req.setAttribute("errorMessage", "Invalid auth credentials!");
            }
            log.debug("welcome() returning view: " + LOGIN_JSP_NAME);
            return new View(LOGIN_JSP_NAME);
        }
    }

    @Action(name=UPLOAD)
    public View upload(HttpServletRequest req) {
        String hasHotDeployment =
                (String) configContext.getAxisConfiguration().getParameterValue("hotdeployment");
        String hasHotUpdate =
                (String) configContext.getAxisConfiguration().getParameterValue("hotupdate");
        req.setAttribute("hotDeployment", (hasHotDeployment.equals("true")) ? "enabled"
                : "disabled");
        req.setAttribute("hotUpdate", (hasHotUpdate.equals("true")) ? "enabled" : "disabled");
        return new View("upload.jsp");
    }

    @Action(name="doUpload", post=true)
    public Redirect doUpload(HttpServletRequest req) throws ServletException {
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
                            return new Redirect(UPLOAD).withStatus(false, "Unsupported file type " + fileExtesion);
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

                            if (!fileNameOnly.matches(FILENAME_REGEX_INVALID_CHARS)) {
                                log.error("doUpload() received invalid filename, redirecting to: " + WELCOME);
                                return new Redirect(UPLOAD).withStatus(false, "Received invalid filename");
                            }
                            File uploadedFile = new File(serviceDir, fileNameOnly);
                            item.write(uploadedFile);
                            return new Redirect(UPLOAD).withStatus(true, "File " + fileNameOnly + " successfully uploaded");
                        }
                    }
                }
            } catch (Exception e) {
                return new Redirect(UPLOAD).withStatus(false, "The following error occurred: " + e.getMessage());
            }
        }
        throw new ServletException("Invalid request");
    }

    @Action(name="login", authorizationRequired=false, post=true, sessionCreationAllowed=true)
    public Redirect login(HttpServletRequest req) {
        // Session fixation prevention: don't allow to login in an existing session.
        // Note that simply invalidating the session and creating a new one is not sufficient
        // because on some servlet containers, the new session will keep the existing session ID.
        if (req.getSession(false) != null) {
            return new Redirect(WELCOME);
        }

        String username = req.getParameter("userName");
        String password = req.getParameter("password");

        if (username != null && !username.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("login() received invalid 'username' param, redirecting to: " + WELCOME);
            return new Redirect(WELCOME).withParameter("failed", "true");
        }

        if (password != null && !password.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("login() received invalid 'password' param, redirecting to: " + WELCOME);
            return new Redirect(WELCOME).withParameter("failed", "true");
        }

        if ((username == null) || (password == null) || username.trim().length() == 0
                || password.trim().length() == 0) {
            return new Redirect(WELCOME).withParameter("failed", "true");
        }

        String adminUserName = (String) configContext.getAxisConfiguration().getParameter(
                Constants.USER_NAME).getValue();
        String adminPassword = (String) configContext.getAxisConfiguration().getParameter(
                Constants.PASSWORD).getValue();

        if (username.equals(adminUserName) && password.equals(adminPassword)) {
            req.getSession().setAttribute(Constants.LOGGED, "Yes");
            return new Redirect(INDEX);
        } else {
            return new Redirect(WELCOME).withParameter("failed", "true");
        }
    }

    @Action(name=EDIT_SERVICE_PARAMETERS)
    public View editServiceParameters(HttpServletRequest req) throws AxisFault {
        String serviceName = req.getParameter("axisService");
        log.debug("editServiceParameters() received 'axisService' param value: " + serviceName);
        if (serviceName == null) {
            log.error("editServiceParameters() received null 'axisService' param, redirecting to: editServiceParameters.jsp");
            req.setAttribute("status", "invalid axisService");
            return new View("editServiceParameters.jsp");
        }
        if (serviceName != null && !serviceName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("editServiceParameters() received invalid 'axisService' param, redirecting to: editServiceParameters.jsp");
            req.setAttribute("status", "invalid axisService");
            return new View("editServiceParameters.jsp");
        }
        AxisService service =
                configContext.getAxisConfiguration().getServiceForActivation(serviceName);
        if (service.isActive()) {

            if (serviceName != null) {
                req.getSession().setAttribute(Constants.SERVICE,
                                              configContext.getAxisConfiguration().getService(
                                                      serviceName));
            }
            req.setAttribute("serviceName", serviceName);
            req.setAttribute("parameters", getParameters(service));
            Map<String,Map<String,String>> operations = new TreeMap<String,Map<String,String>>();
            for (Iterator<AxisOperation> it = service.getOperations(); it.hasNext(); ) {
                AxisOperation operation = it.next();
                operations.put(operation.getName().getLocalPart(), getParameters(operation));
            }
            req.setAttribute("operations", operations);
        } else {
            req.setAttribute("status", "Service " + serviceName + " is not an active service" +
                    ". \n Only parameters of active services can be edited.");
        }
        return new View("editServiceParameters.jsp");
    }

    private static Map<String,String> getParameters(AxisDescription description) {
        Map<String,String> parameters = new TreeMap<String,String>();
        for (Parameter parameter : description.getParameters()) {
            if (parameter.getParameterType() != Parameter.OM_PARAMETER) {
                Object value = parameter.getValue();
                if (value instanceof String) {
                    parameters.put(parameter.getName(), (String)value);
                }
            }
        }
        return parameters;
    }

    @Action(name="updateServiceParameters", post=true)
    public Redirect updateServiceParameters(HttpServletRequest request) throws AxisFault {
        String serviceName = request.getParameter("axisService");
        if (serviceName != null && !serviceName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("updateServiceParameters() received invalid 'serviceName' param, redirecting to: " + EDIT_SERVICE_PARAMETERS);
            return new Redirect(EDIT_SERVICE_PARAMETERS).withStatus(false, "invalid serviceName");
        }
        AxisService service = configContext.getAxisConfiguration().getService(serviceName);
        if (service != null) {
            for (Parameter parameter : service.getParameters()) {
                String para = request.getParameter(serviceName + "_" + parameter.getName());
                if (para != null && !para.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
                    log.error("updateServiceParameters() received invalid param '" +serviceName + "_" + parameter.getName()+ "', redirecting to: " + EDIT_SERVICE_PARAMETERS);
                    return new Redirect(EDIT_SERVICE_PARAMETERS).withStatus(false, "invalid parameter name");
                }
                service.addParameter(new Parameter(parameter.getName(), para));
            }

            for (Iterator<AxisOperation> iterator = service.getOperations(); iterator.hasNext();) {
                AxisOperation axisOperation = iterator.next();
                String op_name = axisOperation.getName().getLocalPart();

                for (Parameter parameter : axisOperation.getParameters()) {
                    String para = request.getParameter(op_name + "_" + parameter.getName());
                    if (para != null && !para.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
                        log.error("updateServiceParameters() received invalid param '" + op_name + "_" + parameter.getName() + "', redirecting to: " + EDIT_SERVICE_PARAMETERS);
                        return new Redirect(EDIT_SERVICE_PARAMETERS).withStatus(false, "invalid parameter name");
                    }

                    axisOperation.addParameter(new Parameter(parameter.getName(), para));
                }
            }
        }
        return new Redirect(EDIT_SERVICE_PARAMETERS)
                .withStatus(true, "Parameters Changed Successfully.")
                .withParameter("axisService", serviceName);
    }

    @Action(name=ENGAGE_GLOBALLY)
    public View engageGlobally(HttpServletRequest req) {
        Map<String,AxisModule> modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);

        req.getSession().setAttribute("modules", null);
        return new View("engageGlobally.jsp");
    }

    @Action(name="doEngageGlobally", post=true)
    public Redirect doEngageGlobally(HttpServletRequest request) {
        String moduleName = request.getParameter("module");
        if (moduleName != null && moduleName != null && !moduleName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("processdisengageModule() received invalid 'moduleName' param, redirecting to: " + LIST_SERVICES);
            return new Redirect(ENGAGE_GLOBALLY).withStatus(false, "invalid moduleName");
        }
        try {
            configContext.getAxisConfiguration().engageModule(moduleName);
            return new Redirect(ENGAGE_GLOBALLY).withStatus(true,
                    moduleName + " module engaged globally successfully");
        } catch (AxisFault axisFault) {
            return new Redirect(ENGAGE_GLOBALLY).withStatus(false, axisFault.getMessage());
        }
    }

    @Action(name=ENGAGE_TO_OPERATION)
    public View engageToOperation(HttpServletRequest req) throws AxisFault {
        Map<String,AxisModule> modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        req.getSession().setAttribute("modules", null);

        String serviceName = req.getParameter("axisService");
        if (serviceName != null && !serviceName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("engageToOperation() received invalid 'serviceName' param, redirecting to: engageToOperation.jsp");
            req.setAttribute("status", "invalid serviceName");
            return new View("engageToOperation.jsp");
        }

        if (serviceName != null) {
            req.setAttribute("service", serviceName);
        }

        req.getSession().setAttribute(
                Constants.OPERATION_MAP,
                configContext.getAxisConfiguration().getService(serviceName).getOperations());

        req.getSession().setAttribute("operation", null);
        return new View("engageToOperation.jsp");
    }

    @Action(name="doEngageToOperation", post=true)
    public Redirect doEngageToOperation(HttpServletRequest request) {
        String moduleName = request.getParameter("module");
        String serviceName = request.getParameter("service");
        String operationName = request.getParameter("axisOperation");
        if (moduleName != null && !moduleName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("doEngageToOperation() received invalid 'moduleName' param, redirecting to: engageToOperation.jsp");
            return new Redirect(ENGAGE_TO_OPERATION).withStatus(false, "invalid moduleName");
        }
        if (serviceName != null && !serviceName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("doEngageToOperation() received invalid 'serviceName' param, redirecting to: engageToOperation.jsp");
            return new Redirect(ENGAGE_TO_OPERATION).withStatus(false, "invalid serviceName");

        }
        if (operationName != null && !operationName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("doEngageToOperation() received invalid 'operationName' param, redirecting to: engageToOperation.jsp");
            return new Redirect(ENGAGE_TO_OPERATION).withStatus(false, "invalid operationName");

        }
        Redirect redirect = new Redirect(ENGAGE_TO_OPERATION).withParameter("axisService", serviceName);
        try {
            AxisOperation od = configContext.getAxisConfiguration().getService(
                    serviceName).getOperation(new QName(operationName));
            od.engageModule(configContext.getAxisConfiguration().getModule(moduleName));
            redirect.withStatus(true, moduleName + " module engaged to the operation successfully");
        } catch (AxisFault axisFault) {
            redirect.withStatus(false, axisFault.getMessage());
        }
        return redirect;
    }

    @Action(name=ENGAGE_TO_SERVICE)
    public View engageToService(HttpServletRequest req) {
        Map<String,AxisModule> modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        populateRequestAttributes(req);


        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        req.getSession().setAttribute("modules", null);

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);

        req.getSession().setAttribute("axisService", null);
        return new View("engageToService.jsp");
    }

    @Action(name="doEngageToService", post=true)
    public Redirect doEngageToService(HttpServletRequest request) {
        String moduleName = request.getParameter("module");
        String serviceName = request.getParameter("axisService");
        if (moduleName != null && !moduleName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("doEngageToService() received invalid 'moduleName' param, redirecting to: engageToOperation.jsp");
            return new Redirect(ENGAGE_TO_SERVICE).withStatus(false, "invalid module name");
        }
        if (serviceName != null && !serviceName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("doEngageToService() received invalid 'serviceName' param, redirecting to: engageToOperation.jsp");
            return new Redirect(ENGAGE_TO_SERVICE).withStatus(false, "invalid serviceName");

        }
        try {
            configContext.getAxisConfiguration().getService(serviceName).engageModule(
                    configContext.getAxisConfiguration().getModule(moduleName));
            return new Redirect(ENGAGE_TO_SERVICE).withStatus(true,
                    moduleName + " module engaged to the service successfully");
        } catch (AxisFault axisFault) {
            return new Redirect(ENGAGE_TO_SERVICE).withStatus(false, axisFault.getMessage());
        }
    }

    @Action(name=ENGAGE_TO_SERVICE_GROUP)
    public View engageToServiceGroup(HttpServletRequest req) {
        Map<String,AxisModule> modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);

        Iterator<AxisServiceGroup> services = configContext.getAxisConfiguration().getServiceGroups();

        req.getSession().setAttribute(Constants.SERVICE_GROUP_MAP, services);

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);
        req.getSession().setAttribute("modules", null);

        req.getSession().setAttribute(Constants.ENGAGE_STATUS, null);

        req.getSession().setAttribute("axisService", null);
        return new View("engageToServiceGroup.jsp");
    }

    @Action(name="doEngageToServiceGroup", post=true)
    public Redirect doEngageToServiceGroup(HttpServletRequest request) throws AxisFault {
        String moduleName = request.getParameter("module");
        String serviceName = request.getParameter("axisService");
        if (moduleName != null && !moduleName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("doEngageToServiceGroup() received invalid 'moduleName' param, redirecting to: engageToOperation.jsp");
            return new Redirect(ENGAGE_GLOBALLY).withStatus(false, "invalid module name");
        }
        if (serviceName != null && !serviceName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("doEngageToServiceGroup() received invalid 'serviceName' param, redirecting to: engageToOperation.jsp");
            return new Redirect(ENGAGE_TO_SERVICE).withStatus(false, "invalid serviceName"); 

        }
        configContext.getAxisConfiguration().getServiceGroup(serviceName).engageModule(
                configContext.getAxisConfiguration().getModule(moduleName));
        return new Redirect(ENGAGE_TO_SERVICE_GROUP).withStatus(true,
                moduleName + " module engaged to the service group successfully");
    }

    @Action(name=LOGOUT)
    public Redirect logout(HttpServletRequest req) {
        req.getSession().invalidate();
        return new Redirect(WELCOME);
    }

    @Action(name="viewServiceGroupContext")
    public View viewServiceGroupContext(HttpServletRequest req) {
        String type = req.getParameter("TYPE");
        String sgID = req.getParameter("ID");
	if (type != null && !type.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("viewServiceGroupContext() received invalid 'type' param, redirecting to: viewServiceGroupContext.jsp");
            req.setAttribute("status", "invalid type");
            return new View("viewServiceGroupContext.jsp");

        }
        if (sgID != null && !sgID.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("viewServiceGroupContext() received invalid 'sgID' param, redirecting to: viewServiceGroupContext.jsp");
            req.setAttribute("status", "invalid sgID");
            return new View("viewServiceGroupContext.jsp");

        }
        ServiceGroupContext sgContext = configContext.getServiceGroupContext(sgID);
        req.getSession().setAttribute("ServiceGroupContext",sgContext);
        req.getSession().setAttribute("TYPE",type);
        req.getSession().setAttribute("ConfigurationContext",configContext);
        return new View("viewServiceGroupContext.jsp");
    }

    @Action(name="viewServiceContext")
    public View viewServiceContext(HttpServletRequest req) throws AxisFault {
        String type = req.getParameter("TYPE");
        String sgID = req.getParameter("PID");
        String ID = req.getParameter("ID");
	if (type != null && !type.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("viewServiceContext() received invalid 'type' param, redirecting to: viewServiceGroupContext.jsp");
            req.setAttribute("status", "invalid type");
            return new View("viewServiceGroupContext.jsp");

        }
        if (sgID != null && !sgID.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("viewServiceContext() received invalid 'sgID' param, redirecting to: viewServiceGroupContext.jsp");
            req.setAttribute("status", "invalid sgID");
            return new View("viewServiceGroupContext.jsp");

        }
        if (ID != null && !ID.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("viewServiceContext() received invalid 'ID' param, redirecting to: viewServiceGroupContext.jsp");
            req.setAttribute("status", "invalid ID");
            return new View("viewServiceGroupContext.jsp");

        }
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
    public View selectServiceParaEdit(HttpServletRequest req) {
        populateRequestAttributes(req);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "SERVICE_PARAMETER");
        req.setAttribute("action", EDIT_SERVICE_PARAMETERS);
        return new View(SELECT_SERVICE_JSP_NAME);
    }

    @Action(name="listOperation")
    public View listOperation(HttpServletRequest req) {
        populateRequestAttributes(req);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "MODULE");
        req.setAttribute("action", ENGAGE_TO_OPERATION);
        return new View(SELECT_SERVICE_JSP_NAME);
    }

    @Action(name=ACTIVATE_SERVICE)
    public View activateService(HttpServletRequest req) {
        populateRequestAttributes(req);
        return new View("activateService.jsp");
    }

    @Action(name="doActivateService", post=true)
    public Redirect doActivateService(HttpServletRequest request) throws AxisFault {
        String serviceName = request.getParameter("axisService");
        if (serviceName != null && !serviceName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("doActivateService() received invalid 'serviceName' param, redirecting to: " + ACTIVATE_SERVICE);
            request.setAttribute("status", "invalid serviceName");
            return new Redirect(ACTIVATE_SERVICE);

        }
        String turnon = request.getParameter("turnon");
        if (turnon != null && !turnon.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("doActivateService() received invalid 'turnon' param, redirecting to: " + ACTIVATE_SERVICE);
            request.setAttribute("status", "invalid turnon");
            return new Redirect(ACTIVATE_SERVICE);

        }
        if (serviceName != null) {
            if (turnon != null) {
                configContext.getAxisConfiguration().startService(serviceName);
            }
        }
        return new Redirect(ACTIVATE_SERVICE);
    }

    @Action(name=DEACTIVATE_SERVICE)
    public View deactivateService(HttpServletRequest req) {
        populateRequestAttributes(req);
        return new View("deactivateService.jsp");
    }

    @Action(name="doDeactivateService", post=true)
    public Redirect doDeactivateService(HttpServletRequest request) throws AxisFault {
        String serviceName = request.getParameter("axisService");
        String turnoff = request.getParameter("turnoff");
        if (serviceName != null && !serviceName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("doDeactivateService() received invalid 'serviceName' param, redirecting to: " + DEACTIVATE_SERVICE);
            request.setAttribute("status", "invalid serviceName");
            return new Redirect(DEACTIVATE_SERVICE);

        }
        if (turnoff != null && !turnoff.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("doDeactivateService() received invalid 'turnoff' param, redirecting to: " + DEACTIVATE_SERVICE);
            request.setAttribute("status", "invalid turnoff");
            return new Redirect(DEACTIVATE_SERVICE);

        }
        if (serviceName != null) {
            if (turnoff != null) {
                configContext.getAxisConfiguration().stopService(serviceName);
            }
        }
        return new Redirect(DEACTIVATE_SERVICE);
    }

    @Action(name="viewGlobalChains")
    public View viewGlobalChains(HttpServletRequest req) {
        req.getSession().setAttribute(Constants.GLOBAL_HANDLERS,
                                      configContext.getAxisConfiguration());

        return new View("viewGlobalChains.jsp");
    }

    @Action(name=VIEW_OPERATION_SPECIFIC_CHAINS)
    public View viewOperationSpecificChains(HttpServletRequest req) throws AxisFault {
        String service = req.getParameter("axisService");
        if (service != null && !service.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("viewOperationSpecificChains() received invalid 'axisService' param, redirecting to: viewOperationSpecificChains.jsp");
            req.setAttribute("status", "invalid axisService");
            return new View("viewOperationSpecificChains.jsp");

        }

        if (service != null) {
            req.getSession().setAttribute(Constants.SERVICE_HANDLERS,
                                          configContext.getAxisConfiguration().getService(service));
        }

        return new View("viewOperationSpecificChains.jsp");
    }

    @Action(name="listPhases")
    public View listPhases(HttpServletRequest req) {
        PhasesInfo info = configContext.getAxisConfiguration().getPhasesInfo();
        req.getSession().setAttribute(Constants.PHASE_LIST, info);
        return new View("viewphases.jsp");
    }

    @Action(name="listServiceGroups")
    public View listServiceGroups(HttpServletRequest req) {
        Iterator<AxisServiceGroup> serviceGroups = configContext.getAxisConfiguration().getServiceGroups();
        populateRequestAttributes(req);
        req.getSession().setAttribute(Constants.SERVICE_GROUP_MAP, serviceGroups);

        return new View("listServiceGroups.jsp");
    }

    @Action(name=LIST_SERVICES)
    public View listServices(HttpServletRequest req) {
        populateRequestAttributes(req);
        req.getSession().setAttribute(Constants.ERROR_SERVICE_MAP,
                                      configContext.getAxisConfiguration().getFaultyServices());

        return new View("listServices.jsp");
    }

    @Action(name="listSingleService")
    public View listSingleService(HttpServletRequest req) throws AxisFault {
        req.getSession().setAttribute(Constants.IS_FAULTY, ""); //Clearing out any old values.
        String serviceName = req.getParameter("serviceName");
        if (serviceName != null && !serviceName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("listSingleService() received invalid 'serviceName' param, redirecting to: listSingleService.jsp");
            req.setAttribute("status", "invalid serviceName");
            return new View("listSingleService.jsp");

        }
        if (serviceName != null) {
            AxisService service = configContext.getAxisConfiguration().getService(serviceName);
            req.getSession().setAttribute(Constants.SINGLE_SERVICE, service);
        }
        return new View("listSingleService.jsp");
    }

    @Action(name="viewContexts")
    public View viewContexts(HttpServletRequest req) {
        req.getSession().setAttribute(Constants.CONFIG_CONTEXT, configContext);
        return new View("viewContexts.jsp");
    }

    @Action(name="globalModules")
    public View globalModules(HttpServletRequest req) {
        Collection<AxisModule> modules = configContext.getAxisConfiguration().getEngagedModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);

        return new View("globalModules.jsp");
    }

    @Action(name="listModules")
    public View listModules(HttpServletRequest req) {
        Map<String,AxisModule> modules = configContext.getAxisConfiguration().getModules();

        req.getSession().setAttribute(Constants.MODULE_MAP, modules);
        req.getSession().setAttribute(Constants.ERROR_MODULE_MAP,
                                      configContext.getAxisConfiguration().getFaultyModules());

        return new View("listModules.jsp");
    }

    @Action(name="disengageModule", post=true)
    public Redirect processdisengageModule(HttpServletRequest req) throws AxisFault {
        String type = req.getParameter("type");
        String serviceName = req.getParameter("serviceName");
        String moduleName = req.getParameter("module");
	if (type != null && !type.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("processdisengageModule() received invalid 'type' param, redirecting to: " + LIST_SERVICES);
            return new Redirect(LIST_SERVICES).withStatus(false, "invalid type");

        }
        if (serviceName != null && !serviceName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("processdisengageModule() received invalid 'serviceName' param, redirecting to: " + LIST_SERVICES);
            return new Redirect(LIST_SERVICES).withStatus(false, "invalid serviceName");

        }
        if (moduleName != null && !moduleName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("processdisengageModule() received invalid 'moduleName' param, redirecting to: " + LIST_SERVICES);
            return new Redirect(LIST_SERVICES).withStatus(false, "invalid moduleName");
        }
        AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
        AxisService service = axisConfiguration.getService(serviceName);
        AxisModule module = axisConfiguration.getModule(moduleName);
        if (type.equals("operation")) {
            if (service.isEngaged(module.getName()) ||
                    axisConfiguration.isEngaged(module.getName())) {
                return new Redirect(LIST_SERVICES).withStatus(false, "Can not disengage module "
                        + moduleName + ". This module is engaged at a higher level.");
            } else {
                String opName = req.getParameter("operation");
                if (opName != null && !opName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
                    log.error("processdisengageModule() received invalid 'operation' param, redirecting to: " + LIST_SERVICES);
                    return new Redirect(LIST_SERVICES).withStatus(false, "invalid operation");
                }
                AxisOperation op = service.getOperation(new QName(opName));
                op.disengageModule(module);
                return new Redirect(LIST_SERVICES).withStatus(true,
                        "Module " + moduleName + " was disengaged from " + "operation " + opName
                                + " in service " + serviceName + ".");
            }
        } else {
            if (axisConfiguration.isEngaged(module.getName())) {
                return new Redirect(LIST_SERVICES).withStatus(false, "Can not disengage module "
                        + moduleName + ". " + "This module is engaged at a higher level.");
            } else {
                service.disengageModule(axisConfiguration.getModule(moduleName));
                return new Redirect(LIST_SERVICES).withStatus(true, "Module " + moduleName
                        + " was disengaged from" + " service " + serviceName + ".");
            }
        }
    }

    @Action(name="deleteService", post=true)
    public Redirect deleteService(HttpServletRequest req) throws AxisFault {
        String serviceName = req.getParameter("serviceName");
        if (serviceName != null && !serviceName.matches(HTTP_PARAM_VALUE_REGEX_WHITELIST_CHARS)) {
            log.error("deleteService() received invalid 'serviceName' param, redirecting to: " + LIST_SERVICES);
            return new Redirect(LIST_SERVICES).withStatus(false, "Failed to delete service '" + serviceName + "'. Received invalid 'serviceName'.");

        }
        AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
        if (axisConfiguration.getService(serviceName) != null) {
            axisConfiguration.removeService(serviceName);
            return new Redirect(LIST_SERVICES).withStatus(true, "Service '" + serviceName + "' has been successfully removed.");
        } else {
            return new Redirect(LIST_SERVICES).withStatus(false, "Failed to delete service '" + serviceName + "'. Service doesn't exist.");
        }
    }

    @Action(name="selectService")
    public View selectService(HttpServletRequest req) {
        populateRequestAttributes(req);
        req.getSession().setAttribute(Constants.SELECT_SERVICE_TYPE, "VIEW");
        req.setAttribute("action", VIEW_OPERATION_SPECIFIC_CHAINS);
        return new View(SELECT_SERVICE_JSP_NAME);
    }
}
