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


package org.apache.axis2.description;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.wsdl.writer.WOMWriter;
import org.apache.axis2.wsdl.writer.WOMWriterFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLConstants;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class AxisService
 */
public class AxisService
        implements ParameterInclude, DescriptionConstants {
    private Definition definition = null;
    private Log log = LogFactory.getLog(getClass());
    private String fileName = "";

    private HashMap operationsAliasesMap = null;
    private HashMap operations = new HashMap();

    // to store module ref at deploy time parsing
    private ArrayList moduleRefs = new ArrayList();

    // to store engaged mdodules
    private ArrayList engagedModules = new ArrayList();
    private String serviceDescription;

    // to store the wsdl definition , which is build at the deployment time
    // to keep the time that last update time of the service
    private long lastupdate;
    private HashMap moduleConfigmap;
    private String name;
    private ParameterInclude parameterInclude;
    private AxisServiceGroup parent;
    private ClassLoader serviceClassLoader;

    //to keep the XMLScheam getting either from WSDL or java2wsdl
    private XmlSchema schema;

    //wsdl is there for this service or not (in side META-INF)
    private boolean wsdlfound = false;

    //to store the scope of the service
    private String scope;

    //to store default message receivers
    private HashMap messageReceivers;
    
    // to store policies which are valid for the entire service
    private PolicyInclude policyInclude;

    /**
     * Constructor AxisService
     */
    public AxisService() {
        this.parameterInclude = new ParameterIncludeImpl();
        this.operationsAliasesMap = new HashMap();
        moduleConfigmap = new HashMap();
        //by dafault service scope is for the request
        scope = Constants.SCOPE_REQUEST;
        messageReceivers = new HashMap();
    }

    /**
     * Constructor AxisService
     */
    public AxisService(String name) {
        this();
        this.name = name;
    }

    public void addMessageReceiver(String mepURL, MessageReceiver messageReceiver) {
        messageReceivers.put(mepURL, messageReceiver);
    }

    public MessageReceiver getMessageReceiver(String mepURL) {
        return (MessageReceiver) messageReceivers.get(mepURL);
    }

    /**
     * Adding module configuration , if there is moduleConfig tag in service
     *
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
        moduleConfigmap.put(moduleConfiguration.getModuleName(), moduleConfiguration);
    }

    /**
     * To add a operation to a service if a module requird to do so
     *
     * @param module
     */
    public void addModuleOperations(ModuleDescription module, AxisConfiguration axisConfig)
            throws AxisFault {
        HashMap map = module.getOperations();
        Collection col = map.values();

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            AxisOperation axisOperation = copyOperation((AxisOperation) iterator.next());
            ArrayList wsamappings = axisOperation.getWsamappingList();

            for (int j = 0; j < wsamappings.size(); j++) {
                Parameter parameter = (Parameter) wsamappings.get(j);

                this.mapActionToOperation((String) parameter.getValue(), axisOperation);
            }

            // this opration is a control operation.
            axisOperation.setControlOperation(true);
            this.addOperation(axisOperation);
        }
    }

    public void addModuleref(QName moduleref) {
        moduleRefs.add(moduleref);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.AxisService#addOperation(org.apache.axis2.description.AxisOperation)
     */

    /**
     * Method addOperation
     *
     * @param axisOperation
     */
    public void addOperation(AxisOperation axisOperation) {
        axisOperation.setParent(this);

        Iterator modules = getEngagedModules().iterator();

        while (modules.hasNext()) {
            ModuleDescription module = (ModuleDescription) modules.next();
            AxisServiceGroup parent = getParent();
            AxisConfiguration axisConfig = null;

            if (parent != null) {
                axisConfig = parent.getParent();
            }

            try {
                axisOperation.engageModule(module, axisConfig);
            } catch (AxisFault axisFault) {
                log.info("Trying to engage a module which is already engege:"
                        + module.getName().getLocalPart());
            }
        }

        operations.put(axisOperation.getName(), axisOperation);
        operationsAliasesMap.put(axisOperation.getName().getLocalPart(), axisOperation);
    }

    /**
     * Method addParameter
     *
     * @param param
     */
    public void addParameter(Parameter param) throws AxisFault {
        if (param == null) {
            return;
        }

        if (isParameterLocked(param.getName())) {
            throw new AxisFault("Parmter is locked can not overide: " + param.getName());
        } else {
            parameterInclude.addParameter(param);
        }
    }

    /**
     * To get a copy from module operation
     *
     * @param axisOperation
     * @return
     * @throws AxisFault
     */
    private AxisOperation copyOperation(AxisOperation axisOperation) throws AxisFault {
        AxisOperation operation =
                AxisOperationFactory.getOperationDescription(axisOperation.getMessageExchangePattern());

        operation.setMessageReceiver(axisOperation.getMessageReceiver());
        operation.setName(axisOperation.getName());

        Iterator parameters = axisOperation.getParameters().iterator();

        while (parameters.hasNext()) {
            Parameter parameter = (Parameter) parameters.next();

            operation.addParameter(parameter);
        }

        operation.setWsamappingList(axisOperation.getWsamappingList());
        operation.setRemainingPhasesInFlow(axisOperation.getRemainingPhasesInFlow());
        operation.setPhasesInFaultFlow(axisOperation.getPhasesInFaultFlow());
        operation.setPhasesOutFaultFlow(axisOperation.getPhasesOutFaultFlow());
        operation.setPhasesOutFlow(axisOperation.getPhasesOutFlow());

        return operation;
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        parameterInclude.deserializeParameters(parameterElement);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.AxisService#addToengagedModules(javax.xml.namespace.QName)
     */

    /**
     * To ebgage a module it is reuired to use this method
     *
     * @param moduleref
     */
    public void engageModule(ModuleDescription moduleref, AxisConfiguration axisConfig)
            throws AxisFault {
        if (moduleref == null) {
            return;
        }

        boolean needToadd = true;
        Iterator itr_engageModules = engagedModules.iterator();

        while (itr_engageModules.hasNext()) {
            ModuleDescription module = (ModuleDescription) itr_engageModules.next();

            if (module.getName().equals(moduleref.getName())) {
                log.debug(moduleref.getName().getLocalPart()
                        + " module has already been engaged on the service. "
                        + " Operation terminated !!!");
                needToadd = false;
            }
        }

        // adding module operations
        addModuleOperations(moduleref, axisConfig);

        Iterator operations = getOperations().values().iterator();

        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();

            axisOperation.engageModule(moduleref, axisConfig);
        }

        if (needToadd) {
            engagedModules.add(moduleref);
        }
    }

    /**
     * Map an action (ala WSA action) to the given operation. This is used by
     * addressing based dispatching to figure out which operation it is that a
     * given message is for.
     *
     * @param action        the action key
     * @param axisOperation the operation to map to
     */
    public void mapActionToOperation(String action, AxisOperation axisOperation) {
        operationsAliasesMap.put(action, axisOperation);
    }

    public void printWSDL(Writer out, String PortURL) throws AxisFault {
        try {
            Definition wsdlDefinition = this.getWSDLDefinition();

            if (wsdlDefinition != null) {
                Collection services = wsdlDefinition.getServices().values();

                for (Iterator iterator = services.iterator(); iterator.hasNext();) {
                    Service service = (Service) iterator.next();
                    Collection ports = service.getPorts().values();

                    for (Iterator iterator1 = ports.iterator(); iterator1.hasNext();) {
                        Port port = (Port) iterator1.next();

                        service.setQName(new QName(this.getName()));

                        SOAPAddress soapAddress = new SOAPAddressImpl();

                        soapAddress.setElementType(SOAPConstants.Q_ELEM_SOAP_ADDRESS);
                        soapAddress.setLocationURI(PortURL);
                        port.getExtensibilityElements().clear();
                        port.addExtensibilityElement(soapAddress);
                    }
                }

                WSDLFactory.newInstance().newWSDLWriter().writeWSDL(wsdlDefinition, out);
                out.flush();


            } else {
                WSDLFactory.newInstance().newWSDLWriter().writeWSDL(wsdlDefinition, out);
                out.write("<wsdl>This service does not have a WSDL</wsdl>");
                out.flush();
            }
        } catch (WSDLException e) {
            throw new AxisFault(e);
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    public void printWSDL(OutputStream out, String serviceURL) throws AxisFault {
        //todo : This is a tempory hack pls imporve me : Deepal
        AxisService2WOM axisService2WOM = new AxisService2WOM(getSchema(), this, null, null, serviceURL);
        try {
            WSDLDescription desc = axisService2WOM.generateWOM();
            
            // populate it with policy information ..
            //TODO : This gives an NPE , Sanka pls fix that
//            PolicyUtil.populatePolicy(desc, this);
            
            WOMWriter womWriter = WOMWriterFactory.createWriter(WSDLConstants.WSDL_1_1);
            womWriter.setdefaultWSDLPrefix("wsdl");
            womWriter.writeWOM(desc, out);

        } catch (Exception e) {
            throw new AxisFault(e);
        }
    }

    /**
     * To get the description about the service which is sepcified in services.xml
     *
     * @return String
     */
    public String getServiceDescription() {
        return serviceDescription;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.AxisService#getClassLoader()
     */

    /**
     * Method getClassLoader
     *
     * @return ClassLoader
     */
    public ClassLoader getClassLoader() {
        return this.serviceClassLoader;
    }

    /**
     * To get the control operation which are added by module like RM
     */
    public ArrayList getControlOperations() {
        Iterator op_itr = getOperations().values().iterator();
        ArrayList operationList = new ArrayList();

        while (op_itr.hasNext()) {
            AxisOperation operation = (AxisOperation) op_itr.next();

            if (operation.isControlOperation()) {
                operationList.add(operation);
            }
        }

        return operationList;
    }

    /**
     * Method getEngadgedModules
     *
     * @return Collection
     */
    public Collection getEngagedModules() {
        return engagedModules;
    }

    public String getFileName() {
        return fileName;
    }

    public long getLastupdate() {
        return lastupdate;
    }

    public ModuleConfiguration getModuleConfig(QName moduleName) {
        return (ModuleConfiguration) moduleConfigmap.get(moduleName);
    }

    public ArrayList getModules() {
        return moduleRefs;
    }

    public String getName() {
        return name;
    }

    /**
     * Method getOperation
     *
     * @param operationName
     * @return AxisOperation
     */
    public AxisOperation getOperation(QName operationName) {
        AxisOperation axisOperation = (AxisOperation) operations.get(operationName);

        if (axisOperation == null) {
            axisOperation = (AxisOperation) operationsAliasesMap.get(operationName.getLocalPart());
        }

        return axisOperation;
    }

    /**
     * Return the AxisOperation which has been mapped to the given action.
     *
     * @param action the action key
     * @return the corresponding AxisOperation or null if it isn't found
     */
    public AxisOperation getOperationByAction(String action) {
        return (AxisOperation) operationsAliasesMap.get(action);
    }

    /**
     * This method will return the operation given particular SOAP Action. This
     * method should only be called if there is only one Endpoint is defined for
     * this Service. If more than one Endpoint exists one of them will be
     * picked. If more than one Operation is found with the given SOAP Action;
     * null will be ruturned. If no particular Operation is found with the given
     * SOAP Action; null will be returned.
     *
     * @param soapAction SOAP Action defined for the particular Operation
     * @return A AxisOperation if a unque Operation can be found with the given
     *         SOAP Action otherwise will return null.
     */
    public AxisOperation getOperationBySOAPAction(String soapAction) {
        if ((soapAction == null) || soapAction.equals("")) {
            return null;
        }

        AxisOperation operation = (AxisOperation) operations.get(new QName(soapAction));

        if (operation != null) {
            return operation;
        }

        operation = (AxisOperation) operationsAliasesMap.get(soapAction);

        return operation;
    }

    /**
     * Method getOperations
     *
     * @return HashMap
     */
    public HashMap getOperations() {
        return operations;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.ParameterInclude#getParameter(java.lang.String)
     */

    /**
     * Method getParameter
     *
     * @param name
     * @return Parameter
     */
    public Parameter getParameter(String name) {
        return parameterInclude.getParameter(name);
    }

    public ArrayList getParameters() {
        return parameterInclude.getParameters();
    }

    /**
     * To get the parent (which is AxisConfiguration in this case)
     *
     * @return <code>AxisConfiguration</code>
     */
    public AxisServiceGroup getParent() {
        return parent;
    }

    /**
     * To get only the publish operations
     */
    public ArrayList getPublishedOperations() {
        Iterator op_itr = getOperations().values().iterator();
        ArrayList operationList = new ArrayList();

        while (op_itr.hasNext()) {
            AxisOperation operation = (AxisOperation) op_itr.next();

            if (!operation.isControlOperation()) {
                operationList.add(operation);
            }
        }

        return operationList;
    }

    public Definition getWSDLDefinition() {
        return definition;
    }

    // to check whether a given parameter is locked
    public boolean isParameterLocked(String parameterName) {

        // checking the locked value of parent
        boolean loscked = false;

        if (getParent() != null) {
            loscked = getParent().getAxisDescription().isParameterLocked(parameterName);
        }

        if (loscked) {
            return true;
        } else {
            Parameter parameter = getParameter(parameterName);

            return (parameter != null) && parameter.isLocked();
        }
    }

    /**
     * Set the description about the service wchih is specified in services.xml
     *
     * @param serviceDescription
     */
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.AxisService#setClassLoader(java.lang.ClassLoader)
     */

    /**
     * Method setClassLoader
     *
     * @param classLoader
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.serviceClassLoader = classLoader;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * This method will set the current time as last update time of the service
     */
    public void setLastupdate() {
        lastupdate = new Date().getTime();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(AxisServiceGroup parent) {
        this.parent = parent;
    }

    public void setWSDLDefinition(Definition difDefinition) {
        this.definition = difDefinition;
    }

    public XmlSchema getSchema() {
        return schema;
    }

    public void setSchema(XmlSchema schema) {
        //todo : need to support multiple schemas
        this.schema = schema;
    }

    public boolean isWsdlfound() {
        return wsdlfound;
    }

    public void setWsdlfound(boolean wsdlfound) {
        this.wsdlfound = wsdlfound;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        if (Constants.SCOPE_APPLICATION.equals(scope) ||
                Constants.SCOPE_TRANSPORT_SESSION.equals(scope) ||
                Constants.SCOPE_SOAP_SESSION.equals(scope) ||
                Constants.SCOPE_REQUEST.equals(scope)) {
            this.scope = scope;
        }
    }
    
    public void setPolicyInclude(PolicyInclude policyInclude) {
        this.policyInclude = policyInclude;
    }
    
    public PolicyInclude getPolicyInclude() {
        return policyInclude;
    }
}
